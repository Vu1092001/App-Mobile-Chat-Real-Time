var socket = io("http://localhost:3000");
var OTP_gui = "";

socket.on("KetQua", function (data) {
  if (data.noiDung == true) {
    alert("đổi mật khẩu thành công");
  }else{
    alert("Đổi mật khẩu thất bại")
  }
  document.getElementById("form").onsubmit("return true");
  document.getElementById("form").submit();
});
socket.on("KQCheckSDT", function (data) {
  alert(data);
});

$(document).ready(function () {
  document.getElementById("divpass").style.display = 'none';
  document.getElementById("divpass1").style.display = 'none';
  document.getElementById("dv1").style.display = 'none';
  document.getElementById("btnQuenMK").style.display= 'none';
  document.getElementById("btnXacThuc").style.display = 'none';

  $("#btnQuenMK").click(function () {
    var tk = $("#edtTk");
    var mk = $("#edtMk");
    var mk2 = $("#edtNLMk");
    var OTP_nhan = $("#edtOTP");

    const vnf_regex = /((09|03|07|08|05)+([0-9]{8})\b)/g;

    if (mk.val() == "" && mk.val() == "") {
      alert("Thông tin đăng ký không được rống!");
      document.getElementById("edtMk").focus();
    } 
    else {
      if(mk.val() == mk2.val()){
        var User = new Object();
             User.sdt = tk.val();
             User.matKhau = mk.val();
             socket.emit("QuenMK", JSON.stringify(User));
      }else {
        alert("Nhập lại mật khẩu không chính sác")
      }      
    }
  });
  $("#OTP").click(function () {
    OTP_gui = Math.floor(Math.random() * 999999) + 111111;
    var tk = $("#edtTk");
     document.getElementById("dv1").style.display = 'block';
     document.getElementById("btnXacThuc").style.display = 'block';
    var code = new Object();
    code.sdt = tk.val();
    code.code = OTP_gui;

    socket.emit("sendOTP", JSON.stringify(code));
  });
  $("#btnXacThuc").click(function () {
    var OTP_nhan = $("#edtOTP");
    if(OTP_nhan.val() == OTP_gui){
      document.getElementById("divpass").style.display = 'block';
      document.getElementById("divpass1").style.display = 'block';
      document.getElementById("dv").style.display = 'none';
      document.getElementById("dv1").style.display = 'none';
      document.getElementById("btnQuenMK").style.display = 'block';
      document.getElementById("btnXacThuc").style.display = 'none';
      document.getElementById("OTP").style.display = 'none';
    }else{
      alert('chịu');
    }
  });
});
