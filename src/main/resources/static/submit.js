function doSubmit(form) {
  var chart = {}
  chart.type = "FlowChart"
  chart.items = []

  for (idx in itemDivs) {
    var div = itemDivs[idx]
    var item = createItemFromDiv(div)
    chart.items.push(item)
  }
//  var rect = {}
//  rect.type = "rectangle"
//  rect.height = 20
//  rect.width = 20
//  rect.x = 10
//  rect.y = 10
//  chart.items.push(rect)

  console.log(chart)
  var json = JSON.stringify(chart)
  console.log(json)
  document.getElementById("response").src = "/diagram?d=" + encodeURIComponent(json)
}

function createItemFromDiv(div) {
  var item = {}
  console.log("div: ", div)
  for (childIdx in div.childNodes) {

    var attributeNode = div.childNodes[childIdx]
    if (attributeNode == null) {
      continue
    }
    if (attributeNode.tagName == "INPUT" || attributeNode.tagName == "SELECT") {
      var attributeName = attributeNameFromId(attributeNode.id)
      if (attributeNode.value.length > 0) {
        item[attributeName] = attributeNode.value;
      }
    }
  }
  return item;
}

function attributeNameFromId(id) {
  if (id.startsWith("item-")) {
    id = id.substring(5);
    id = id.substring(0, id.lastIndexOf("-"))
    return id;
  }
  return null;
}

function newItem() {
  var items = document.getElementById("items");
  var itemDiv = createItemDiv();
  items.appendChild(itemDiv);
}

var itemDivs = []
var nextItemId = 1;
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

  var idInput = document.createElement("input")
  idInput.id = "item-id-" + itemId
  addOption(div, "ID (Opt)", idInput)

  var typeInput = document.createElement("select")
  typeInput.id = "item-type-" + itemId
  var rectOpt = document.createElement("option")
  rectOpt.value = "rectangle"
  rectOpt.innerText = "Rectangle"
  typeInput.appendChild(rectOpt)
  addOption(div, "Type", typeInput)

  var heightInput = document.createElement("input")
  heightInput.id = "item-height-" + itemId
  addOption(div, "Height", heightInput);

  var widthInput = document.createElement("input")
  widthInput.id = "item-width-" + itemId
  addOption(div, "Width", widthInput);

  var xInput = document.createElement("input")
  xInput.id = "item-x-" + itemId
  addOption(div, "X coor (Opt)", xInput);

  var yInput = document.createElement("input")
  yInput.id = "item-y-" + itemId
  addOption(div, "Y coor (Opt)", yInput);

  var locationReferenceInput = document.createElement("input")
  locationReferenceInput.id = "item-y-" + itemId
  addOption(div, "Loc Ref (Opt)", locationReferenceInput);

  var fillColorInput = document.createElement("input")
  fillColorInput.id = "item-fillColor-" + itemId
  addOption(div, "Fill Color (Opt)", fillColorInput);

  var outlineColorInput = document.createElement("input")
  outlineColorInput.id = "item-outlineColor-" + itemId
  addOption(div, "Outline Color (Opt)", outlineColorInput);

  div.appendChild(document.createElement("br"))
  return div;
}

function addOption(div, label, input) {
  var idLabel = document.createElement("text")
  idLabel.innerText = label
  div.appendChild(idLabel);
  div.appendChild(input);
  div.appendChild(document.createElement("br"))
}

function deleteItemDiv(div) {
  console.log("Deleting ", div);
  const index = itemDivs.indexOf(div);
  if (index > -1) {
    itemDivs.splice(index, 1);
  }
  let parent = div.parentNode
  parent.removeChild(div)
}