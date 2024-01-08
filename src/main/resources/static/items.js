function createItemFromDiv(div) {
  var item = {}

  for (let i = 0; i < div.childNodes.length; i++) {

    var attributeNode = div.childNodes[i]
    if (attributeNode.id == null || attributeNode.id.length == 0) {
      continue
    }
    var attributeName = attributeNameFromId(attributeNode.id)
    var inputValue = document.getElementById(attributeNode.id + "-input").value
    if (inputValue.length > 0) {
      item[attributeName] = inputValue;
    }
  }
  return item;
}

var itemDivs = []
var nextItemId = 1;

function newItem() {
  var items = document.getElementById("items");
  var itemDiv = createItemDiv();
  items.appendChild(itemDiv);
}

function createItemDiv() {
  var div = document.createElement("div")
  div.itemId = nextItemId;
  nextItemId++;
  itemDivs.push(div)

  var itemId = div.itemId

  var delLink = document.createElement("a")
  delLink.innerText = "X"
  delLink.setAttribute("href", "#")
  delLink.setAttribute("onclick","deleteItemDiv(this.parentNode)");
  delLink.style.color = "red"
  delLink.style.paddingRight = "5px"
  div.appendChild(delLink)

  var typeInput = document.createElement("select")
  typeInput.setAttribute("onchange", "hideIrrelevantOptions(this)")
  var rectOpt = document.createElement("option")
  rectOpt.value = "rectangle"
  rectOpt.innerText = "Rectangle"
  typeInput.appendChild(rectOpt)
  var gridOpt = document.createElement("option")
  gridOpt.value = "grid"
  gridOpt.innerText = "Grid"
  typeInput.appendChild(gridOpt)
  var textOpt = document.createElement("option")
  textOpt.value = "text"
  textOpt.innerText = "Text"
  typeInput.appendChild(textOpt)
  optionId = "item-type-" + itemId
  addOption(div, "Type", typeInput, optionId)

  var idInput = document.createElement("input")
  optionId = "item-id-" + itemId
  addOption(div, "ID (Opt)", idInput, optionId)

  var heightInput = document.createElement("input")
  optionId = "item-height-" + itemId
  addOption(div, "Height", heightInput, optionId);

  var widthInput = document.createElement("input")
  optionId = "item-width-" + itemId
  addOption(div, "Width", widthInput, optionId);

  var xInput = document.createElement("input")
  optionId = "item-x-" + itemId
  addOption(div, "X coor (Opt)", xInput, optionId);

  var yInput = document.createElement("input")
  optionId = "item-y-" + itemId
  addOption(div, "Y coor (Opt)", yInput, optionId);

  var locationReferenceInput = document.createElement("input")
  optionId = "item-locationReference-" + itemId
  addOption(div, "Loc Ref (Opt)", locationReferenceInput, optionId);

  var fillColorInput = document.createElement("input")
  optionId = "item-fillColor-" + itemId
  addOption(div, "Fill Color (Opt)", fillColorInput, optionId);

  var outlineColorInput = document.createElement("input")
  optionId = "item-outlineColor-" + itemId
  addOption(div, "Outline Color (Opt)", outlineColorInput, optionId);

  addOption(div, "Span Width", document.createElement("input"), "item-spanWidth-" + itemId);
  addOption(div, "Span Height", document.createElement("input"), "item-spanHeight-" + itemId);

  addOption(div, "Text", document.createElement("input"), "item-text-" + itemId);
  addOption(div, "Font Name (Opt)", document.createElement("input"), "item-fontName-" + itemId);
  addOption(div, "Font Style (Opt)", document.createElement("input"), "item-style-" + itemId);
  addOption(div, "Font Size (Opt)", document.createElement("input"), "item-size-" + itemId);
  addOption(div, "Font Color (Opt)", document.createElement("input"), "item-color-" + itemId);
  addOption(div, "Padding (Opt)", document.createElement("input"), "item-padding-" + itemId);
  addOption(div, "PaddingLeft (Opt)", document.createElement("input"), "item-paddingLeft-" + itemId);
  addOption(div, "PaddingTop (Opt)", document.createElement("input"), "item-paddingTop-" + itemId);

  div.appendChild(document.createElement("br"))

  hideIrrelevantOptions(typeInput)
  return div;
}

const RECTANGLE_PARAMETERS = ["id", "type", "height", "width", "x", "y", "locationReference", "fillColor", "outlineColor"]
const GRID_PARAMETERS = ["id", "type", "x", "y", "locationReference", "spanWidth", "spanHeight"]
const TEXT_PARAMETERS = ["id", "type", "x", "y", "locationReference", "text", "fontName", "style", "size", "color", "padding", "paddingLeft", "paddingTop"]

function hideIrrelevantOptions(typeSelector) {
  var requiredParams = ["type"]
  if (typeSelector.value == "rectangle") {
    requiredParams = RECTANGLE_PARAMETERS
  } else if (typeSelector.value == "grid") {
    requiredParams = GRID_PARAMETERS
  } else if (typeSelector.value == "text") {
    requiredParams = TEXT_PARAMETERS
  }

  div = typeSelector.parentNode.parentNode

  for (let i = 0; i < div.childNodes.length; i++) {

    var attributeNode = div.childNodes[i]
    if (attributeNode.id == null || !attributeNode.id.startsWith("item-")) {
      continue
    }

    var attributeName = attributeNameFromId(attributeNode.id)
    if (requiredParams.includes(attributeName)) {
      attributeNode.hidden = false
    } else {
      attributeNode.hidden = true
      attributeNode.inputField.value = ""
    }
  }
}

function addOption(div, label, input, optionId) {
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

function deleteItemDiv(div) {
  const index = itemDivs.indexOf(div);
  if (index > -1) {
    itemDivs.splice(index, 1);
  }
  let parent = div.parentNode
  parent.removeChild(div)
}

function attributeNameFromId(id) {
  if (id.startsWith("item-")) {
    id = id.substring(5);
    id = id.substring(0, id.lastIndexOf("-"))
    return id;
  }
  return null;
}
