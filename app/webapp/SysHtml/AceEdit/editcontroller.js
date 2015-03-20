var aceEditModule;
var LOCSTORE = "recent";


aceEditModule = angular.module("AceEdit", [])
.config(function(){
    
});

aceEditModule.controller("EditController", ['$scope',
        function($scope){
    
    $scope.recentfls = [];
    
    $scope.rtnload = function(event){
        if ( event.which == 13 ) { //Look for return
            event.preventDefault();
            $scope.loadfile();
         }
    }  
    
    
    $scope.menuload = function(event){
        event.preventDefault();
        
        console.log("flpath : "+ event.currentTarget.innerText);
        $("#filename").val(event.currentTarget.innerText);
        $scope.loadfile();
    }
    
    
    $scope.loadfile = function() {

    var flnm = getFileName();
    
    if (flnm.length < 9){
        alertmodal("File path name needs to be at least 9 characters");
    } else if (invalidExt(flnm)) {
        alertmodal("File extension must be .js, .txt, or .html");
    } else {
    
        $.ajax({
        	type: "GET",
        	url: "http://" + $("#hostname").val() + flnm,
            dataType: "text",
        	success: function(rcv){
        		        var flnm = $("#filename").val();
        		        if (flnm.indexOf(".js") > 0){
        		          editor.getSession().setMode("ace/mode/javascript");
        		        } else if (flnm.indexOf(".html") > 0){
        		        editor.getSession().setMode("ace/mode/html");
        		        } else {
        		            editor.getSession().setMode("ace/mode/text");
        		        }

        			    editor.getSession().setValue(rcv);
        			    setPathStore($("#filename").val());
        			    $scope.$apply($scope.getRecent); //refresh list
        			    
        			    var title = flnm.split("/");
        			    if (title.length > 0){
        			        $("title").text(title[title.length-1]);
        			    }
        		    },
        	error: function(err,texts){
        		        alertmodal("Load error : " + texts)
        		    }
            });
        }
    }
    
    
    $scope.getRecent = function(){
        var cnt = 0;
        var key;
        var val;
    
        key = LOCSTORE + cnt;
        $scope.recentfls = [];
        
        while (cnt < 10 && (val = localStorage.getItem(key)) !== null)
        {
            $scope.recentfls[cnt] = JSON.parse(val);
            console.log("In getRecent loop path : " + $scope.recentfls[cnt].path + "  cnt : "+cnt);
            ++cnt;
            key = LOCSTORE + cnt;
        }
        
   //     $scope.$apply();
    };

    $scope.getRecent();
}]);



function setPathStore(flpath){
    
    var cnt = 0;
    var key;
    var val;
    var keyoldest, timeoldest;
    
    key = LOCSTORE + cnt;
    
    while (cnt < 10)
    {
        if ((val = localStorage.getItem(key)) === null){
            localStorage.setItem(key, makeRecent(flpath));
            console.log("Insert new item : "+ key + "  : "+ flpath)
            return;
        } else {
            val = JSON.parse(val);
            if (val.path == flpath){
                localStorage.setItem(key, makeRecent(flpath));
                console.log("Update item : "+ key + "  : "+ flpath)
                return;
            } else {
                if (cnt == 0) {
                    keyoldest = key;
                    timeoldest = val.time;
                } else if (val.time < timeoldest){
                    keyoldest = key;
                    timeoldest = val.time;
                }
            }
        }
         ++cnt;
        key = LOCSTORE + cnt;
        console.log("Looping bottom of while : "+ key);
    }
    
    localStorage.setItem(keyoldest, makeRecent(flpath));
}


function makeRecent(flpath){
    
    return(JSON.stringify({path: flpath, time: new Date().getTime()}))
}

