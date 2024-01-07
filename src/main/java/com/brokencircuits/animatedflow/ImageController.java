package com.brokencircuits.animatedflow;

import com.brokencircuits.animatedflow.dsl.Diagram;
import com.brokencircuits.animatedflow.dsl.DiagramNodeTransformation;
import com.brokencircuits.animatedflow.dsl.DiagramRectangle;
import com.brokencircuits.animatedflow.dsl.FlowChart;
import com.brokencircuits.animatedflow.dsl.NodeTextConfig;
import com.brokencircuits.animatedflow.evaluator.FlowChartEvaluator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ImageController {

  private final FlowChartEvaluator flowChartEvaluator;

  @GetMapping(path = "/test", produces = MediaType.IMAGE_GIF_VALUE)
  public byte[] test() throws IOException {
    FlowChart chart = new FlowChart();
    chart.setHeight(400);
    chart.setWidth(600);

    chart.setItems(List.of(
        DiagramRectangle.builder()
            .id("o2")
            .width(30)
            .height(30)
            .fillColor("BLACK")
            .x(60)
            .y(50)
            .text("O1")
            .textConfig(NodeTextConfig.builder()
                .build())
            .build()
    ));
    chart.setTransforms(List.of(
        new DiagramNodeTransformation("o2", Duration.ofSeconds(5), Duration.ofSeconds(6), 150,
            50)));
    return createDiagram(chart);
  }

  @PostMapping(path = "/diagram2", produces = MediaType.IMAGE_GIF_VALUE)
  public byte[] createDiagram(@RequestBody Diagram diagram) throws IOException {

    if (!(diagram instanceof FlowChart chart)) {
      throw new IllegalStateException("Unsupported chart type");
    }

    Iterator<ImageWriter> gif = ImageIO.getImageWritersByFormatName("gif");
    ImageWriter writer = gif.next();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageOutputStream os = ImageIO.createImageOutputStream(baos);
    writer.setOutput(os);
    writer.prepareWriteSequence(null);

    for (DiagramFrame frame : flowChartEvaluator.renderAnimated(chart)) {
      BufferedImage img = frame.getImage();
      ImageTypeSpecifier imgType = ImageTypeSpecifier.createFromBufferedImageType(img.getType());
      IIOMetadata metadata = writer.getDefaultImageMetadata(imgType, null);
      setAnimationLoop(metadata);

      String nativeMetadataFormatName = metadata.getNativeMetadataFormatName();
      Node asTree = metadata.getAsTree(nativeMetadataFormatName);
      NodeList childNodes = asTree.getChildNodes();
      for (int j = 0; j < childNodes.getLength(); j++) {
        Node delayTimeNode = childNodes.item(j).getAttributes().getNamedItem("delayTime");
        if (delayTimeNode != null) {
          delayTimeNode.setNodeValue(String.valueOf(frame.getDuration().toMillis() / 10));
        }
      }

      metadata.setFromTree(nativeMetadataFormatName, asTree);
      IIOImage temp = new IIOImage(img, null, metadata);

      writer.writeToSequence(temp, null);
    }

    writer.endWriteSequence();
    os.flush();

    return baos.toByteArray();
  }

  private static void setAnimationLoop(IIOMetadata m) throws IIOInvalidTreeException {
    String format = m.getNativeMetadataFormatName();
    Node root = m.getAsTree(format);

    IIOMetadataNode extentions = (IIOMetadataNode)
        getChildNode(root, "ApplicationExtensions");
    if (extentions == null) {
      extentions = new IIOMetadataNode("ApplicationExtensions");
      root.appendChild(extentions);
    }
    IIOMetadataNode loop = (IIOMetadataNode)
        getChildNode(extentions, "ApplicationExtension");
    if (loop == null) {
      loop = new IIOMetadataNode("ApplicationExtension");
      extentions.appendChild(loop);
    }
    loop.setAttribute("applicationID", "NETSCAPE");
    loop.setAttribute("authenticationCode", "2.0");
    loop.setUserObject(new byte[]{1, 0, 0});

    m.setFromTree(format, root);
  }

  private static Node getChildNode(Node n, String name) {
    Node c = n.getFirstChild();
    while (c != null && !name.equals(c.getNodeName())) {
      c = c.getNextSibling();
    }
    return c;
  }
}
