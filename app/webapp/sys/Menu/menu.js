var menuModule;

menuModule = angular.module("menuApp", [])
.config(function(){

   console.log("In config ");

});

menuModule.controller("MenuController", ['$scope',
    function($scope){
					console.log("In controller");

    	$scope.findall = function() {
	 			getMenu(function(jobj){
				   console.log("In getMenu");

   				 if (jobj[0].rtn && jobj[0].db > 0) {
						 jobj.shift();
       	  	 $scope.sites = jobj;
						 $scope.$apply();
    			 }
				 });
			 }

    $scope.findall();

    $scope.showitem = function(site){
        return(site.id != -1);
    }

		$scope.newurl = function(site){
			location.assign(site.href);
		}

		$scope.delSite = function(site){
			if (!isLocal()) {
				alert("Can remove site only from phone");
			} else if (confirm("Continue to delete : " + site.app)) {
				if (android.removeSite(site.id)){
					site.id = -1;
				}
			}
		}

}]);

function isLocal(){

    return(typeof android !== "undefined" && typeof android.removeSite === "function")
}
