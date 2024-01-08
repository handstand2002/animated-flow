function doSubmit(form) {
  var chart = {}
  chart.type = "FlowChart"
  chart.items = []
  chart.transforms = []

  for (idx in itemDivs) {
    var div = itemDivs[idx]
    var item = createItemFromDiv(div)
    chart.items.push(item)
  }

  for (let i = 0; i < transformDivs.length; i++) {
    var div = transformDivs[i]
    var xform = createTransformFromDiv(div)
    chart.transforms.push(xform)
  }

  console.log(chart)
  var json = JSON.stringify(chart)
//  console.log(json)
  document.getElementById("response").src = "/diagram?d=" + encodeURIComponent(json)
}
