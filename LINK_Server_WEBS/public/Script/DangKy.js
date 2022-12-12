var socket = io("http://localhost:3000");
var OTP_gui = "";

socket.on("ketQua", function (data) {
  alert(data.noiDung);
  if (data.noiDung == true) {
    alert("Đăng ký thành công");
    document.getElementById("form").onsubmit("return true");
    document.getElementById("form").submit();
  } else {
    alert("Đăng ký thât bại");
  }
});

socket.on("loiSDT", function (data) {
  if (data == false) {
    alert("Số điện thoại không tồn tại");
  }
});

$(document).ready(function () {
  $("#btnDangKy").click(function () {
    var tk = $("#edtTk");
    var mk = $("#edtMk");
    var mk2 = $("#edtNLMk");
    var tenTK = $("#edtTTk");
    var OTP_nhan = $("#edtOTP");

    const vnf_regex = /((09|03|07|08|05)+([0-9]{8})\b)/g;

    if (
      tk.val() == "" ||
      mk.val() == "" ||
      mk2.val() == "" ||
      tenTK == "" ||
      OTP_nhan == ""
    ) {
      alert("Thông tin đăng ký không được rống!");
      document.getElementById("edtTk").focus();
    } else if (vnf_regex.exec(tk.val()) == null) {
      alert("Số điện thoại của bạn không đúng định dạng!");
      document.getElementById("edtTk").focus();
    } else if (mk.val() < 8) {
      alert("Mật khẩu phải từ 8 ký tự trở lên");
      document.getElementById("edtMk").focus();
    } else if (mk.val() != mk2.val()) {
      alert("Mật khẩu nhập lại SAI");
      document.getElementById("edtNLMk").focus();
    } else if (tenTK < 10) {
      alert("Tên tài khoản phải từ 10 ký tự trở lên");
      document.getElementById("edtTTk").focus();
    } else {
      if (mk.val() == mk2.val()) {
        if (OTP_nhan.val() == OTP_gui) {
          var User = new Object();
          User.sdt = tk.val();
          User.matKhau = mk.val();
          User.name = tenTK.val();

          socket.emit("DangKy", JSON.stringify(User));
        } else alert("Sai Mã OTP");
      } else {
        alert("Kiểm tra thông tin mk");
      }
    }
  });
  $("#OTP").click(function () {
    OTP_gui = Math.floor(Math.random() * 999999) + 111111;
    var tk = $("#edtTk");

    var code = new Object();
    code.sdt = tk.val();
    code.code = OTP_gui;

    socket.emit("sendOTP", JSON.stringify(code));
  });
});
