package com.brokencircuits.animatedflow;

import com.brokencircuits.animatedflow.dsl.Diagram;
import com.brokencircuits.animatedflow.dsl.FlowChart;
import com.brokencircuits.animatedflow.evaluator.FlowChartEvaluator;
import com.brokencircuits.animatedflow.spring.GenericPropertyEditor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ImageController {

  private final FlowChartEvaluator flowChartEvaluator;
  private final ObjectMapper objectMapper;

  // support using diagram as request parameter
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Diagram.class,
        new GenericPropertyEditor<>(objectMapper, Diagram.class));
  }

  @GetMapping(path = "/diagram", produces = MediaType.IMAGE_GIF_VALUE)
  public byte[] createDiagram(@RequestParam Diagram d) throws IOException {

    if (!(d instanceof FlowChart chart)) {
      throw new IllegalStateException("Unsupported chart type");
    }
    log.info("Diagram: {}", d);

    Iterator<ImageWriter> gif = ImageIO.getImageWritersByFormatName("gif");
    ImageWriter writer = gif.next();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageOutputStream os = ImageIO.createImageOutputStream(baos);
    writer.setOutput(os);
    writer.prepareWriteSequence(null);

    for (DiagramFrame frameDetails : flowChartEvaluator.renderAnimated(chart)) {
      BufferedImage img = frameDetails.getImage();
      ImageTypeSpecifier imgType = ImageTypeSpecifier.createFromBufferedImageType(img.getType());

      IIOMetadata metadata = writer.getDefaultImageMetadata(imgType, null);
      updateFrameMetadata(chart, frameDetails, metadata);
      IIOImage frame = new IIOImage(img, null, metadata);

      writer.writeToSequence(frame, null);
    }

    writer.endWriteSequence();
    os.flush();

    return baos.toByteArray();
  }

  private void updateFrameMetadata(FlowChart chart, DiagramFrame frame, IIOMetadata metadata)
      throws IIOInvalidTreeException {
    if (chart.isLoop()) {
      setAnimationLoop(metadata);
    }

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
