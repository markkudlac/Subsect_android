

function initbut() {

console.log("In initbut client.js");

 $("#lastnamebut").on("click", alertbut)

}


function alertbut(){

    alert("This is client.js")
    processimg($("#pic1"), "/img/mark_head2.jpg")
}