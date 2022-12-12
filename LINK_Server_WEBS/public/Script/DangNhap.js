var socket = io("http://localhost:3000");

socket.on("ketQua", function (data) {
  if (data.noiDung == true) {
    document.getElementById("form").onsubmit("return true");
    document.getElementById("form").submit();
  } else {
    alert("Đăng nhập thât bại");
  }
});

socket.on("userOnl", function (data) {
  socket.emit("ImOnline", data);
  // alert(JSON.stringify(data));
});

$(document).ready(function () {
  $("#btnDangNhap").click(function () {
    var tk = $("#txtTK");
    var mk = $("#txtMK");

    const vnf_regex = /((09|03|07|08|05)+([0-9]{8})\b)/g;

    if (tk.val() == "" || mk.val() == "") alert("Nhập dủ dữ liệu");
    else if (vnf_regex.exec(tk.val()) == null) {
      alert("Số điện thoại của bạn không đúng định dạng!");
      document.getElementById("txtTK").focus();
    } else if (mk.val() < 8) {
      alert("Mật khẩu phải từ 8 ký tự trở lên");
      document.getElementById("txtMK").focus();
    } else {
      var User = new Object();
      User.sdt = tk.val();
      User.matKhau = mk.val();

      socket.emit("DangNhap", JSON.stringify(User));
    }
  });
});
