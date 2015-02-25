
	var editor;


    function aceinit() {
        editor = ace.edit("editor");
        editor.setTheme("ace/theme/eclipse");
        editor.getSession().setMode("ace/mode/javascript");
      }


	function initUI(){

		$('#savebut').on('click', function() {
		$.post("http://"+$("#hostname").val()+"/api/savefile", 'filename=' +$("#filename").val()+
		'&filecontent='+
		encodeURIComponent(editor.getSession().getValue()), function(rcv){

		    if (!JSON.parse(rcv).rtn) alert("File save error");
        });
	    });


	    $('#loadbut').on('click', function() {

		$.get("http://" + $("#hostname").val()+'/'+$("#filename").val(), function(rcv){
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
		});
	    });

	    $("#hostname").val(window.location.host);
	    $("#filename").val(localStorage.getItem("lastfile"));
	}
