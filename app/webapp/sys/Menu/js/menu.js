

angular.module("menuApp", ['ngRoute'])

.config(function($routeProvider){
    $routeProvider.when("/login/:target", {
        templateUrl: "login.html",
		controller: "LoginController"
    }).when("/download", {
        templateUrl: "download.html",
		controller: "DownloadController"
    }).when("/upload", {
        templateUrl: "upload.html",
		controller: "UploadController"
    }).otherwise( {
        templateUrl: "main.html",
		controller: "MenuController"
    });
	
})

.factory('utility', function() {
	var utility = {};
	
	utility.testApply = function(scope){
		if(!scope.$$phase) {
			scope.$apply();
		}
	}
	
	return utility;
})

.controller("MenuController", ['$scope', '$location',
    function($scope, $location){

    function setTitle(){
        
        var title = "";
    
        if (isLocal()) {
            title = "Installed"
            $("body").removeClass("bodynavpad");
        } else {
            title = (window.location.host.split("."))[0];
        }
        $(".settitle").text(title);
    }


    function isLocal(){
  
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
        return(site.id != -1 && site.permissions.charAt(2) == 'F');
    }

    $scope.showremove = function(){
        return(isLocal());
    }


	$scope.newurl = function(site){
		location.assign(site.href);
	}


	$scope.delSite = function(site) {
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
	
	$scope.toLogin = function() {
	    $location.path("/login");
	}
	
	
	$scope.toDownload = function() {
	    $location.path("/download");
	}
	
		
	$scope.toUpload = function() {
	    $location.path("/upload");
	}
	
	setTitle();
    findall();
}])

/**************************************/
.controller("LoginController", ['$scope', '$routeParams', '$location', '$window',
	function($scope, $routeParams, $location, $window){	
		
	$scope.passwdbuf=""

	function manageLogin(target, loc){	

		if (!loggedIn()) {
	//		loc.path("/" + target);
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
//					loggedIn = passwd;
					manageLogin($routeParams.target, $location);
				} else  {
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
		$window.history.back();
	}
	
	manageLogin($routeParams.target, $location);
}])

/**************************************/
.controller("DownloadController", ['$scope', 'utility', '$location', '$window', '$sce',
	function($scope, utility, $location, $window, $sce){	
	
	$scope.dirfiles = [];
	
	function dirFile(ipadd){
	
    $.ajax({
    	type: "GET",
    	url: "http://" + ipadd + "/backup",
        dataType: "text",
    	success: function(rcv){
    	    
			var jqobj = $(rcv);
			var ahref;
			
			$scope.dirfiles = [];

			jqobj.filter('a').each(function() {
			    ahref = $(this).attr("href");
			    $(this).attr("href", "http://" + ipadd + ahref);
//					console.log ("Get the ancs 3 : " + $(this).prop('outerHTML'));

					$scope.dirfiles.push($sce.trustAsHtml(
					    $(this).prop('outerHTML')));
				});
				
				utility.testApply($scope);
			
    		 },
    	error: function(err,texts){
			alertmodal("Load error : " + texts);
    		 }
        });
	}
	
	
	
	$scope.downloadCancel = function(){
		
		$window.history.back();
	}
	
	
	getIPadd(function(rtn){
	    dirFile(rtn[1].ipadd);
	});
	
}])

/**************************************/
.controller("UploadController", ['$scope', 'utility', '$location', '$window', '$sce',
	function($scope, utility, $location, $window, $sce){	
	
	$scope.open = {directory: "/", dirfiles: []};
	$scope.ipadd = null;
	$scope.uploadsrc = null;
	
	var loadcount = 0;  //This forces reload of iframe
	
	function setDirectory(dir){
	    setUploadDirectory(dir, function(rtn) {
	        console.log("Set upload rtn : " + rtn[0].rtn);
	        if (rtn[0].rtn) {
	            getIPadd(function(rtn){
	                $scope.ipadd = rtn[1].ipadd;
	            $scope.uploadsrc = $sce.trustAsResourceUrl(
	                        "http://" + $scope.ipadd + "/sys/Menu/upload.html?cnt=" + loadcount);
	            ++loadcount;
	            utility.testApply($scope);
	            });
	        }
	    })
	}
	
	
	$scope.dirfile = function(){
		
        $.ajax({
        	type: "GET",
        	url: "http://" + $scope.ipadd + $scope.open.directory,
            dataType: "text",
        	success: function(rcv){
				var jqobj = $(rcv);
				var litext;
//	alert("Got : " + rcv)			
				$scope.open.directory = jqobj.filter('h1').text().split(" ")[1];
				$scope.open.dirfiles = [];
	console.log("Directory : " + $scope.open.directory);		
				jqobj.find('a').add(jqobj.filter('a')).each(function(){
						console.log ("Get the ancs1 : " + $(this).text());
						litext = $(this).text() == ".." ? "<-BACK" : $(this).text();
						if (litext.endsWith("/") || litext == "<-BACK") {
						    $scope.open.dirfiles.push({name: litext});
						}
					});
					
					utility.testApply($scope);
					$("#dirfile").modal('show');
        		 },
        	error: function(err,texts){
				alertmodal("Load error : " + texts);
        		 }
            });
	}
	
	
	$scope.dirfileload = function(event){
        event.preventDefault();
        
		var str = event.currentTarget.innerText
		
		if (str.endsWith("/")) {
			$scope.open.directory = $scope.open.directory + str;
			$scope.dirfile();
		} else if (str == "<-BACK"){
			$scope.open.directory = $scope.open.directory.replace(/\w+\/$/, "");
			if ($scope.open.directory.length <= 0) $scope.open.directory = "/";
			$scope.dirfile();
		} else {
			setTimeout(function(){alertmodal("Directory ERROR")}, 200);
		}
	}
	
	
	$scope.selectDir = function(){
	    
	    var xdir = $scope.open.directory.replace(/^\//, "");
	    xdir = xdir.replace(/\/$/, "");
	    
	    console.log("Selected dir is : " + xdir)
	    
	    if (xdir.length >= 3) {
	        setDirectory(xdir);
	    } else {
	        setTimeout(function(){alertmodal("Invalid Directory for Upload")}, 200);
	    }
	}
	
	
	$scope.uploadCancel = function(){
		
		$window.history.back();
	}
	
	setDirectory("restore");
	
}]);


