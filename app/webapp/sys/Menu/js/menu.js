

angular.module("Menu", ['ngRoute'])

.config(function($routeProvider){
    $routeProvider.when("/login", {
        templateUrl: "login.html",
    }).when("/download", {
        templateUrl: "download.html",
		controller: "DownloadController"
    }).when("/upload", {
        templateUrl: "upload.html",
		controller: "UploadController"
	}).when("/help", {
            templateUrl: "help_menu.html",
    		controller: "HelpController"
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

    $scope.sites = [];
    
    function setTitle(){
        
        $(".settitle").text(window.location.host.split(".")[0]);
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
        return(site.id != -1 && 
        (site.permissions.charAt(2) == 'F' || loggedIn()));
    }


    $scope.showlog = function(){
        return(loggedIn());
    }
    
    
	$scope.newurl = function(site){
		location.assign(site.href);
	}
	
	
	$scope.toLogin = function() {
	    $location.path("/login");
	}
	
	
	$scope.Logout = function() {
	    remotecall(null, "sys/Menu", "logout", 
	    	null, function(){
	        console.log("logged out");
	    	});
	}
	
	
	$scope.toDownload = function() {
	    $location.path("/download");
	}
	
		
	$scope.toUpload = function() {
	    $location.path("/upload");
	}

	$scope.toHelp = function() {
    	    $location.path("/help");
    	}
	
	setTitle();
    findall();
}])

/**************************************/
.controller("DownloadController", ['$scope', 'utility', '$location', '$sce',
	function($scope, utility, $location, $sce){
	
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
			alertmodal("Load error or no backup generated: " + texts);
    		 }
        });
	}
	
	
	
	$scope.downloadCancel = function(){
		
		$location.replace();
        $location.path('/');
	}
	
	
	getIPadd(function(rtn){
	    dirFile(rtn[1].ipadd);
	});
	
}])

/**************************************/
.controller("UploadController", ['$scope', 'utility', '$location', '$sce',
	function($scope, utility, $location, $sce){
	
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

				$scope.open.directory = jqobj.filter('h1').text().split(" ")[1];
				$scope.open.dirfiles = [];
//	console.log("Directory : " + $scope.open.directory);
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
		
		$location.replace();
        $location.path('/');
	}
	
	
	setDirectory("restore");
	
}])

/**************************************/
.controller("HelpController", ['$scope', 'utility', '$location', '$sce',
	function($scope, utility, $location, $sce){

	$scope.loadhelp = null;

		function sethelp(){
            getsubFile("help_server.html", function(xfle){
                $scope.loadhelp = $sce.trustAsHtml(xfle);
                utility.testApply($scope);
            });

    	}

	$scope.helpCancel = function(){
    		$location.replace();
            $location.path('/');
    	}

	sethelp();
}]);

