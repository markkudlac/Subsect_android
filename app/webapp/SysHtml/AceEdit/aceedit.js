
var editor;
var changed = false;

function aceinit() {
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/eclipse");
    editor.getSession().setMode("ace/mode/javascript");
    
    editor.commands.addCommand({
    name: 'saveFile',
    bindKey: {
    win: 'Ctrl-S',
    mac: 'Command-S',
    sender: 'editor|cli'
    },
    exec: savefile
    });
    
    setSelectedPointsize();
    $('#pointsz').change(setSelectedPointsize);
}


function setSelectedPointsize(){
 
    var sz = $('#pointsz').find('option:selected').text();
    
     editor.setOptions({
        fontSize: sz
    });
}


function getFileName(){
             var tmpfile = $("#filename").val();

            if (tmpfile.charAt(0) != "/") {
                tmpfile = "/" + tmpfile;
            }
            return(tmpfile);
}


function initUI(){

    $(document).on("copy", selectToClip);
 
    $('#newbut').on('click', newfile);
    $('#savebut').on('click', savefile);
    $('#selectallbut').on('click', function(){
        editor.selectAll();
    });
    
    $("#hostname").val(window.location.host);
   
    $('#aceredo').on('click', function(){editor.redo()});
    $('#aceundo').on('click', function(){editor.undo()});
    editor.on("change", docChanged);
}
        	

function savefile() {

    var flnm = getFileName();
    
    if (flnm.length < 9){
        alertmodal("File path name needs to be at least 9 characters");
    } else if (invalidExt(flnm)) {
        alertmodal("File extension must be .js, .txt, or .html");
    } else {
    
        $.post("http://"+$("#hostname").val()+"/api/savefile", 'filename=' + flnm +
    		'&filecontent='+
    		encodeURIComponent(editor.getSession().getValue()), function(rcv){

    		    if (!JSON.parse(rcv).rtn) {
    		        alertmodal("File save error");
    		    } else {
    		        toast("File saved");
    		        clearChanged();
    		    }
            });
    }
}
    

function newfile(){
    
    var flnm = $("#filename").val();
    
    editor.getSession().setValue("");
    $("title").text("New");

    if (flnm.length > 2){
        var xpath = flnm.split("/");
        
        if (xpath.length > 0){
            flnm = flnm.substring(0, flnm.indexOf(xpath[xpath.length-1]));
            if (flnm.length > 0){
                $("#filename").val(flnm)
            }
        }
    }
    clearChanged();
}


 function selectToClip(e){
  e.stopPropagation();
  e.preventDefault();
  
    toast("Copy complete")        
  var cd = e.originalEvent.clipboardData;
  cd.setData("text/plain", editor.getSession().getValue());
 }


function docChanged(){
    
    if (!changed){
        changed = true;
        $("#modicon").removeClass("hidden")
    }
}


function clearChanged(){
    changed = false;
    $("#modicon").addClass("hidden")
}


function invalidExt(flnm){
    return(null === flnm.match(/.+\.js$/) &&
    null === flnm.match(/.+\.txt$/) &&
    null === flnm.match(/.+\.css$/) &&
    null === flnm.match(/.+\.json$/) &&
    null === flnm.match(/.+\.html$/)
    );
}


function alertmodal(str){
    
    $('#alertmess').text(str)
    $('#alertmodal').modal('show')
    
}


function toast(str){
    
    $("#filename").popover({content: str, placement: "bottom", triggetr: "manual"}).popover("show");
    setTimeout(function(){$("#filename").popover("destroy")}, 3000);
}      	    
        	    