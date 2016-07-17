angular.module("Installed",[])

.factory('utility', function() {
	var utility = {};
	
	utility.testApply = function(scope){
		if(!scope.$$phase) {
			scope.$apply();
		}
	}
	
	return utility;
})

.controller("InstalledController", ['$scope',
    function($scope){

    $scope.sites = [];
    
    function isAndroid(){
  
        if (typeof android !== "undefined" &&
            typeof android.removeSite === "function"){
                var target = window.location.hostname + ":" + window.location.port
                return(target == android.subsectHost());
        } else {
            return(false);
        }
    }
    

    function findall() {
 		getMenu(function(jobj){
		 //  console.log("In getMenu");

   		    if (jobj[0].rtn && jobj[0].db > 0) {
				 jobj.shift();
     	             $scope.sites = jobj;
				 $scope.$apply();
		    }
		 });
	 }

    
    $scope.showitem = function(site){
        return(site.id != -1)
    }


	$scope.delSite = function(site) {

		    alertmodal("Continue to remove : " + site.app, function() {

			    if (android.removeSite(site.id)){
				    site.id = -1;
				    $scope.$apply();
			    }
		    });
	}
	
	
	if (isAndroid()) {
        findall();
	} else {
	    alertmodal("Android JavaScript interface failed");
	}
	
}])