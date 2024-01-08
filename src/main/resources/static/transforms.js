function createTransformFromDiv(div) {
  var transform = {}

  for (let i = 0; i < div.childNodes.length; i++) {

    var attributeNode = div.childNodes[i]
    if (attributeNode.id == null || attributeNode.id.length == 0) {
      continue
    }
    var attributeName = transformAttributeNameFromId(attributeNode.id)
    var inputValue = document.getElementById(attributeNode.id + "-input").value
    if (inputValue.length > 0) {
      transform[attributeName] = inputValue;
    }
  }
  return transform;
}

var transformDivs = []
var nextTransformId = 1;

function newTransform() {
  var transforms = document.getElementById("transforms");
  var transformDiv = createTransformDiv();
  transforms.appendChild(transformDiv);
}


function createTransformDiv() {
  var div = document.createElement("div")
  div.transformId = nextTransformId;
  nextTransformId++;
  transformDivs.push(div)

  var transformId = div.transformId

  var delLink = document.createElement("a")
  delLink.innerText = "X"
  delLink.setAttribute("href", "#")
  delLink.setAttribute("onclick","deleteTransformDiv(this.parentNode)");
  delLink.style.color = "red"
  delLink.style.paddingRight = "5px"
  div.appendChild(delLink)

  addTransformOption(div, "Object ID", document.createElement("input"), "transform-objectId-" + transformId)
  addTransformOption(div, "Start Time", document.createElement("input"), "transform-startTime-" + transformId)
  addTransformOption(div, "End Time", document.createElement("input"), "transform-endTime-" + transformId)
  addTransformOption(div, "New X (Opt)", document.createElement("input"), "transform-newX-" + transformId)
  addTransformOption(div, "New Y (Opt)", document.createElement("input"), "transform-newY-" + transformId)
  addTransformOption(div, "New Loc Reference (Opt)", document.createElement("input"), "transform-newLocationReference-" + transformId)

  div.appendChild(document.createElement("br"))
  return div;
}

function addTransformOption(div, label, input, optionId) {
  input.id = optionId + "-input"
  var parentDiv = document.createElement("div")
  parentDiv.id = optionId
  var idLabel = document.createElement("text")

  idLabel.innerText = label
  parentDiv.appendChild(idLabel);
  parentDiv.appendChild(input);
  parentDiv.appendChild(document.createElement("br"))
  parentDiv.inputField = input;
  div.appendChild(parentDiv)
}

function deleteTransformDiv(div) {
  const index = transformDivs.indexOf(div);
  if (index > -1) {
    transformDivs.splice(index, 1);
  }
  let parent = div.parentNode
  parent.removeChild(div)
}

function transformAttributeNameFromId(id) {
  if (id.startsWith("transform-")) {
    id = id.substring("transform-".length);
    id = id.substring(0, id.lastIndexOf("-"))
    return id;
  }
  return null;
}
