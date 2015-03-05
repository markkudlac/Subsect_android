
function initbut() {

// console.log("In initbut client.js");

    $("#imagebut").on("click", imagebut)
    $("#savebut").on("click", savebut)
    $("#findbut").on("click", findbut)
    $("#updatebut").on("click", updatebut)
    $("#removebut").on("click", removebut)
}


function imagebut(){

    processimg($("#pic1")[0], "img/mark_head2.jpg")
}


function savebut() {
    insertDB("device", {tag: $("#lastname").val(), status: "X"})
}


function findbut() {

    queryDB("device", {}, {limit: 23})
}


function updatebut() {

    updateDB("device", {tag: "Its alive", status: "J"}, "id > ?", {id: 4})
}


function removebut() {

    removeDB("device", 'status = "J" AND id > ?', {id: 0})
}