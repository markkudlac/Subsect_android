var testAppModule;

function initang() {
//    console.log("Angular init");

testAppModule = angular.module("testApp", []);

testAppModule.controller("NamesController", ['$scope', function($scope){

    var names = {};

    names.first = "Mark";
    names.last = "Kudlac";

    $scope.names = names;
    $scope.chgname = function(){names.first = "New";
        console.log("clicked but");
    }
}]);

    angular.element(document).ready(function() {
      angular.bootstrap(document, ['testApp']);
    });
}



function initbut() {
// console.log("In initbut client.js");

    initang();

    $("#savebut").on("click", savebut)
    $("#findbut").on("click", findbut)
    $("#updatebut").on("click", updatebut)
    $("#removebut").on("click", removebut)

    $("#imagebut").on("click", imagebut)

    $("#audiobut").on("click", audiobut)
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


function audiobut(){

    processimg($("#audsrc")[0], "img/WickedGrav.mp3")
}


