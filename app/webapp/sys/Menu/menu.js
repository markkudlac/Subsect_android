var menuModule;

menuModule = angular.module("menuApp", [])
.config(function(){

});

menuModule.controller("MenuController", ['$scope',
    function($scope){

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
				alertmodal("Can remove site only from phone");
			} else {
			    alertmodal("Continue to delete : " + site.app, function() {

				    if (android.removeSite(site.id)){
					    site.id = -1;
					    $scope.$apply();
				    }
			    });
			}
		}

}]);


function isLocal(){

    return(typeof android !== "undefined" && typeof android.removeSite === "function")
}


function alertmodal(str, func){

	$("#continueop").off("click").removeClass("hidden");
	$('#modaltitle').removeClass("alert-danger").removeClass("alert-info");
	if (typeof func !== "function"){
		$('#modaltitle').text("Alert").addClass("alert-danger");
		$("#continueop").addClass("hidden")
	} else {
		$('#modaltitle').text("Confirm").addClass("alert-info");
		$("#continueop").one("click", func);
	}

  $('#alertmess').text(str)
  $('#alertmodal').modal('show')
}