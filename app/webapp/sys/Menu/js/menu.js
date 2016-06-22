

angular.module("menuApp", ['ngRoute'])

.config(function($routeProvider){
    $routeProvider.when("/login/:target", {
        templateUrl: "login.html",
		controller: "LoginController"
    }).otherwise( {
        templateUrl: "main.html",
		controller: "MenuController"
    });
	
})

.controller("MenuController", ['$scope',
    function($scope){

    function setTitle(){
        
            var title = "";
    
        if (isLocal()) {
            title = "Installed"
        } else {
            title = (window.location.host.split("."))[0];
        }
        $(".settitle").text(title);
    }


    function isLocal(){
    //    return true;
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
        return(site.id != -1);
    }

    $scope.showremove = function(){
        return(isLocal());
    }


	$scope.newurl = function(site){
		location.assign(site.href);
	}


	$scope.delSite = function(site){
		if (!isLocal()) {
			alertmodal("Can remove site only from phone");
		} else {
		    alertmodal("Continue to remove : " + site.app, function() {

			    if (android.removeSite(site.id)){
				    site.id = -1;
				    $scope.$apply();
			    }
		    });
		}
	}
		
	setTitle();
    findall();
}])

/**************************************/
.controller("LoginController", ['$scope', '$routeParams', '$location', '$window',
	function($scope, $routeParams, $location, $window){	
		
	$scope.passwdbuf=""
		

	function manageLogin(target, loc){	

		if (loggedIn != null) {
			loc.path("/" + target);
			$scope.$apply();
		} 
	}
	
	
    $scope.rtnpasswd = function(event){
        if ( event.which == 13 ) { //Look for return
            event.preventDefault();
            $scope.loginpass($(event.target).val());
         }
    }  
	
	
	$scope.loginpass = function(passwd){
		
		if ($scope.passValid(passwd)){
    		testPassword(passwd, false, function(rcv){
//        		console.log("testPassword return :" + rcv[0].db);

				if (passwd.length < 1){
			        alertmodal("Password blank");
			    } else if (rcv[0].rtn && rcv[0].db == 1) {
					loggedIn = passwd;
					manageLogin($routeParams.target, $location);
				} else  {
					loggedIn = null;
					alertmodal("Incorrect Password");
				}
	    	});
		}
	}
	
	
	$scope.passValid = function(val){
		
		if (tmpstr = val.match(/\W/)) {
			alertmodal("Password has only letters, numbers, or _. No : ' " + tmpstr +" '");
			$scope.passwdbuf = $scope.passwdbuf.replace(/\W/g, '')
			return false
		} else {
			return true
		}
	}
	
	
	$scope.loginCancel = function(){
		loggedIn = null;
		$window.history.back();
	}
	
	manageLogin($routeParams.target, $location);
}]);



/*
function alertmodal(str, func){

	generateAlertHtml();

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


function generateAlertHtml(){

	if ($("#alertmodal").length == 0){
		$('body').append(
'<div id="alertmodal" class="modal fade"><div class="modal-dialog"><div class="modal-content"> \
<div class="modal-header"><h4 id="modaltitle" class="modal-title alert-danger" \
style="padding-left: 10px">Alert</h4></div><div class="modal-body"><p id="alertmess"></p> \
</div><div class="modal-footer"><button class="btn btn-default" data-dismiss="modal"> \
Close</button><button id="continueop" class="btn btn-primary hidden" \
data-dismiss="modal">Continue</button></div></div></div>/div>'
		);
	}
}

*/