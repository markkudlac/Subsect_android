
	var editor;

    function aceinit() {
        editor = ace.edit("editor");
        editor.setTheme("ace/theme/eclipse");
        editor.getSession().setMode("ace/mode/javascript");
      }


	function getFileName(){
             var tmpfile = $("#filename").val();

            if (tmpfile.charAt(0) != "/") {
                tmpfile = "/" + tmpfile;
            }
            return(tmpfile);
        }

    	function initUI(){

    		$('#savebut').on('click', function() {

    		$.post("http://"+$("#hostname").val()+"/api/savefile", 'filename=' + getFileName() +
    		'&filecontent='+
    		encodeURIComponent(editor.getSession().getValue()), function(rcv){

    		    if (!JSON.parse(rcv).rtn) alert("File save error");
            });
    	    });

	     $('#loadbut').on('click', function() {

        		$.ajax({
        		    type: "GET",
        		    url: "http://" + $("#hostname").val() + getFileName(),
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
        			    localStorage.setItem("lastfile", $("#filename").val());
        		    },
        		    error: function(err,texts){
        		        alert("Load error : " + texts)
        		    }
        		  });
        	    });

        	    $("#hostname").val(window.location.host);
        	    $("#filename").val(localStorage.getItem("lastfile"));
        	}