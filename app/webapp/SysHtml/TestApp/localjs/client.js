var testAppModule;
var names = [];

function initang() {

console.log("In initang")


testAppModule = angular.module("testApp", ['ngRoute'])
.config(function($routeProvider){
    
    $routeProvider.when("/additem", {
        templateUrl: "additem.html"
    });
})
.directive('script', ['$templateCache', '$http',
    function($templateCache, $http) {
        return {
            restrict: 'E',
            terminal: true,
            compile: function(element, attr) {
                if (attr.type == 'text/ng-template' && attr.src && !attr.id) {
                    $http.get(attr.src, { cache: $templateCache });
                }
            }
        };
    }
]);



testAppModule.controller("NamesController", ['$scope', '$templateCache', '$location',
        function($scope, $templateCache, $location){

    $scope.master = {};
    
    $scope.findall = function() {
    queryDB("device", {}, {limit: 50}, function(rcv){ 
      //  console.log("testApp ran run 1 db :" + rcv[0].db);
        rcv.shift();
        $scope.names = rcv
        $scope.$apply();
    });
    }
    
    $scope.findall();
    
    $scope.clearall = function() {
        $scope.clearadd();
        $scope.names = [];
    }
    
    
    $scope.newitem = function() {
        $location.path('/additem');
        $scope.user={id: -1}
    }
    
    $scope.savenew = function(user) {
        
        if ($scope.user.id == -1) {
            $scope.master = angular.copy(user);
     //   console.log("name : "+ $scope.master.name + "  STS : " + $scope.master.status)
            insertDB("device", {tag: $scope.master.tag, status: $scope.master.status.substr(0,1)},
            function(rcv){
                if (rcv[0].rtn) {
                    $scope.clearadd();
                    $scope.findall();
                } else {
                    alert("Add failed")
               }
            })
        } else {
            console.log("Save update")
            
            updateDB("device", {tag: user.tag, status: user.status.substr(0,1)}, "",
                {id: user.id}, function(rcv){
                if (rcv[0].rtn) {
                    $scope.clearadd();
                    $scope.findall();
                } else {
                    alert("Add failed")
               }
            })
        }
    }
    
    
    $scope.deleteitem = function(item, index) {
        console.log("Delete : " + item.id + "  index : " + index)
        removeDB("device", '', {id: $scope.names[index].id}, function(){
        $scope.names[index].id = -1;
        console.log("Deleted : " + $scope.names[index].tag)
        $scope.$apply();
        })
    }
    
    
    $scope.edititem = function(item, first) {
       console.log("Edit : " + item.id + "  first : " + first)
        $scope.newitem();
        $scope.user=item;
  
    }
    
    $scope.showitem = function(item){
        return(item.id != -1);
    }
    
    
    $scope.clearadd = function(){
        $location.path('/');
    }
}])

      angular.bootstrap(document, ['testApp']);
    
}


function initbut() {

    initang();

    $("#imagebut").on("click", imagebut)
    $("#audiobut").on("click", audiobut)
}


function imagebut(){

    processimg($("#pic1")[0], "img/mark_head2.jpg")
}


function audiobut(){

    processimg($("#audsrc")[0], "img/WickedGrav.mp3")
}
