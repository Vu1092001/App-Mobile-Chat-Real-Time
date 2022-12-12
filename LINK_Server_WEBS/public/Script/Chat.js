var socket = io("http://localhost:3000");

// 114314199521 | tranthanhnam | N@m123456

// Danh sách biến tạo thêm
var j = 0;
var k = 0;
var lsUser = [];
var lsFriendUser = [];
var lsGroupUser = [];
var lsMess = [];
var nguoiGui = "";
var tenNguoiGui = "";
var nguoiNhan = "";
var nameNguoiNhan = "";
var id = 0;
var soLuongTinNhan = 0;
var soLuongHinhAnh = 0;
var lsTinNhanLuu = [];
var tenNhom = "";
var dataUser;
var type = "";
var khoa;
var lsHinh = [];

$(document).ready(function () {
  $("#Hello").show();
  $("#Left-Chat").show();
  $("#Left-Friend").hide();
  $("#Right-Friend").hide();
  $("#Right-Profile").hide();
  $("#Right-Chat").hide();

  $("#chat").html("");
  $("#HeaderUserChatWith").html("");
  nguoiGui = document.getElementById("userProfile").innerHTML;
  tenNguoiGui = document.getElementById("userNameProfile").innerHTML;
  console.log("Người vừa đăng nhập: " + nguoiGui);
  console.log("Tên vừa đăng nhập: " + tenNguoiGui);
  socket.emit("ImOnline", nguoiGui);
  $("#divProFileUser").append(
    '<h2 class="h2Profile" id="proFileUser">' + tenNguoiGui + "</h2>"
  );

  //yêu cầu lấy list bạn bè từ server
  socket.emit("GetLsBanBe", nguoiGui);
});

// So sánh phần tử có trong chuỗi không (trả về TRUE or FALSE)
function check_arr(element, arr) {
  let count = 0;
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] === element) {
      count++;
      break;
    }
  }
  return count > 0 ? true : false;
}

//Hiển thị danh sách bạn bè
socket.on("lsBanBe", function (datauser, datagroup) {
  j = 0;
  k = 0;
  console.log(datauser);
  console.log(datagroup);
  lsUser = datauser;
  lsGroupUser = datagroup;

  myFunction1();

  var userHaveChat = [];
  lsUser.forEach(function (i) {
    i.lsNhom.forEach(function (k) {
      if (i.name === tenNguoiGui) {
        if (i.sdt === nguoiGui) {
          userHaveChat.push(i.name);
        }
      }
    });
  });

  lsUser.forEach(function (i) {
    if (check_arr(i.name, userHaveChat) === false) {
      $("#boxContent").append(
        '<li onclick="getUser(' +
          j +
          ')">' +
          '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg" alt="">' +
          "<div>" +
          '<h2 id="tenNguoiNhan">' +
          i.name +
          "</h2>" +
          "<h3>" +
          '<span class="status orange"></span>' +
          "offline" +
          "</h3>" +
          "</div>" +
          "</li>"
      );
      lsFriendUser.push(i);
      j++;
    }
  });

  lsGroupUser.forEach(function (i) {
    $("#boxContent").append(
      '<li onclick="getUserGroup(' +
        k +
        ')">' +
        '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg" alt="">' +
        "<div>" +
        '<h2 id="tenNguoiNhan">' +
        i +
        "</h2>" +
        "<h3>" +
        '<span class="status orange"></span>' +
        "offline" +
        "</h3>" +
        "</div>" +
        "</li>"
    );
    k++;
  });
});

// Chờ fix
socket.on("kQTimBanBe", function (data) {
  console.log("Tìm thấy", JSON.stringify(data));
});

//Nhận tin nhắn khi có người gửi tin nhắn tới
socket.on("getMess", function (TinNhan, data, name) {
  if (TinNhan.nguoiGui != nguoiGui) {
    if (data != null) {
      var reader = new FileReader();
      reader.onloadend = function () {
        document
          .getElementById("preview" + id)
          .setAttribute("src", reader.result);
        id++;
      };
      var blob = new Blob([data]);
      reader.readAsDataURL(blob);
      $("#chat").append(
        '<li class="you">' +
          '<div class="entete">' +
          '<span class="status green"></span>' +
          "<h2>" +
          TinNhan.nguoiGui +
          "</h2>" +
          "<h3> &nbsp;10:12AM, Today</h3>" +
          "</div>" +
          '<div class="triangle"></div>' +
          '<img id="preview' +
          id +
          '" class="message">' +
          "</li>"
      );
    } else {
      $("#chat").append(
        '<li class="you">' +
          '<div class="entete">' +
          '<span class="status green"></span>' +
          "<h2>" +
          TinNhan.nguoiGui +
          "</h2>" +
          "<h3> &nbsp;10:12AM, Today</h3>" +
          "</div>" +
          '<div class="triangle"></div>' +
          '<div class="message">' +
          TinNhan.noiDung +
          "</div>" +
          "</li>"
      );
    }
  }
  $("#chat").animate({ scrollTop: 20000000 }, "slow");
});

// Lấy số lượng tin nhắn của user hiện tại
socket.on("loadMess", function (dataText, dataImage) {
  soLuongTinNhan = dataText;
  soLuongHinhAnh = dataImage;
});

// Load tin nhắn trước đã gửi
socket.on("loadMessWEB", function (noiDung, hinhAnh, index) {
  if (type == "User") {
    var TinNhan = new Object();
    TinNhan.nguoiGui = nguoiGui2;
    TinNhan.noiDung = noiDung;
    TinNhan.hinhAnh = hinhAnh;
    TinNhan.index = index;
    lsTinNhanLuu.push(TinNhan);
    if (lsTinNhanLuu.length == soLuongTinNhan) {
      lsTinNhanLuu.sort(function (a, b) {
        let left = a.index;
        let right = b.index;
        return left === right ? 0 : left > right ? 1 : -1;
      });
      for (let i = 0; i < soLuongTinNhan; i++) {
        if (lsTinNhanLuu[i].hinhAnh != "") {
          var reader = new FileReader();
          reader.onloadend = function () {
            document
              .getElementById("preview" + id)
              .setAttribute("src", reader.result);
            id++;
          };
          var blob = new Blob([lsTinNhanLuu[i].hinhAnh]);
          reader.readAsDataURL(blob);
          if (nguoiGui2 == lsTinNhanLuu[i].nguoiGui) {
            $("#chat").append(
              '<li class="me">' +
                '<div class="entete">' +
                '<span class="status green"></span>' +
                "<h2>" +
                tenNguoiGui +
                "</h2>" +
                "<h3> &nbsp;10:12AM, Today</h3>" +
                "</div>" +
                '<div class="triangle"></div>' +
                '<img id="preview' +
                id +
                '" class="message">' +
                "</li>"
            );
          } else {
            $("#chat").append(
              '<li class="you">' +
                '<div class="entete">' +
                '<span class="status green"></span>' +
                "<h2>" +
                nameNguoiNhan +
                "</h2>" +
                "<h3> &nbsp;10:12AM, Today</h3>" +
                "</div>" +
                '<div class="triangle"></div>' +
                '<img id="preview' +
                id +
                '" class="message">' +
                "</li>"
            );
          }
        } else {
          if (nguoiGui2 == lsTinNhanLuu[i].nguoiGui) {
            $("#chat").append(
              '<li class="me">' +
                '<div class="entete">' +
                '<span class="status green"></span>' +
                "<h2>" +
                tenNguoiGui +
                "</h2>" +
                "<h3> &nbsp;10:12AM, Today</h3>" +
                "</div>" +
                '<div class="triangle"></div>' +
                '<div class="message">' +
                lsTinNhanLuu[i].noiDung +
                "</div>" +
                "</li>"
            );
          } else {
            $("#chat").append(
              '<li class="you">' +
                '<div class="entete">' +
                '<span class="status green"></span>' +
                "<h2>" +
                nameNguoiNhan +
                "</h2>" +
                "<h3> &nbsp;10:12AM, Today</h3>" +
                "</div>" +
                '<div class="triangle"></div>' +
                '<div class="message">' +
                lsTinNhanLuu[i].noiDung +
                "</div>" +
                "</li>"
            );
          }
        }
      }
    }
  }
});

// Thông báo đến User được vào nhóm
socket.on("VaoNhom", function (data) {
  alert("Bạn đã được thêm vào nhóm: " + data);

  $("#boxContent").html("");
  $("#myDropdown1").html("");
  //yêu cầu lấy list bạn bè từ server
  socket.emit("GetLsBanBe", nguoiGui);
});

//Hiển thị chat box với người mình chọn
function getUser(data) {
  type = "User";
  $("#Hello").hide();
  $("#Right-chat").show(1000);
  nguoiNhan = lsFriendUser[data].sdt;
  nameNguoiNhan = lsFriendUser[data].name;

  console.log(nguoiNhan);
  console.log(nameNguoiNhan);

  var TinNhan = new Object();
  TinNhan.nguoiGui = nguoiGui;
  TinNhan.nguoiNhan = nguoiNhan;
  socket.emit("loadMess", JSON.stringify(TinNhan));

  $("#chat").html("");
  $("#HeaderUserChatWith").html("");
  $("#HeaderUserChatWith").append(
    '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg" alt="">' +
      "<div>" +
      '<h2 id="ChatWithUser">Chat with ' +
      lsFriendUser[data].name +
      "</h2>" +
      "<h3>already 1902 messages</h3>" +
      "</div>" +
      '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/ico_star.png" alt="">'
  );
}

function getUserGroup(data) {
  type = "Group";
  $("#Hello").hide();
  $("#Right-chat").show(1000);
  tenNhom = lsGroupUser[data];
  console.log(lsGroupUser[data]);

  $("#chat").html("");
  $("#HeaderUserChatWith").html("");
  $("#HeaderUserChatWith").append(
    '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg" alt="">' +
      "<div>" +
      '<h2 id="ChatWithUser">Chat with ' +
      tenNhom +
      "</h2>" +
      "<h3>already 1902 messages</h3>" +
      '<ion-icon name="list-outline" id="list-menu" onclick="openGroup()" ondblclick="closeGroup()"></ion-icon>' +
      "</div>" +
      '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/ico_star.png" alt="">'
  );

  socket.emit("joinNhom", tenNhom);
}

var interval = 1000; // how much time should the delay between two iterations be (in milliseconds)?
var promise = Promise.resolve();

//Gửi tin nhắn tới bạn bè (text, emoji, hình ảnh)
function sendMessage() {
  var sender = nguoiGui;
  var count = 0;
  if (type == "User") {
    var receiver = nguoiNhan;
  } else {
    var receiver = "NhomChat" + tenNhom;
  }
  var txtMess = document.getElementById("txtMessage").value;

  console.log("Người gửi: ", sender);
  console.log("Người nhận: ", receiver);
  console.log("Nội dung chat: ", txtMess);

  // Lấy thời gian hiện tại
  var currentdate = new Date();
  var datetime =
    currentdate.getUTCFullYear() +
    ("0" + (currentdate.getUTCMonth() + 1)).slice(-2) +
    ("0" + currentdate.getUTCDate()).slice(-2) +
    "_" +
    ("0" + currentdate.getHours()).slice(-2) +
    ("0" + currentdate.getMinutes()).slice(-2) +
    ("0" + currentdate.getSeconds()).slice(-2);

  console.log(datetime);

  var tinNhan = new Object();
  tinNhan.nguoiGui = sender;
  tinNhan.nguoiNhan = receiver;
  tinNhan.noiDung = txtMess;
  tinNhan.thoiGianGui = datetime;
  tinNhan.viTriPhanHoi = -1;

  let fileInput = document.getElementById("upload_input");
  var ourFile = document.getElementById("upload_input").files[0];
  if (ourFile != undefined) {
    for (i of fileInput.files) {
      count = count + 1;
    }
    // Gửi nhiều hình
    if (count > 1) {
      tinNhan.slImage = count;
      tinNhan.loai = "lsImage";
      for (y of fileInput.files) {
        let reader = new FileReader();
        reader.onload = () => {
          $("#chat").append(
            '<li class="me">' +
              '<div class="entete">' +
              "<h3>10:12AM, Today </h3>" +
              "<h2> &nbsp;" +
              tenNguoiGui +
              "</h2>" +
              '<span class="status blue"></span>' +
              "</div>" +
              '<div class="triangle"></div>' +
              '<img id="preview' +
              id +
              '" class="message">' +
              "</li>"
          );
          let img = document.getElementById("preview" + id);
          img.setAttribute("src", reader.result);
          id++;
        };
        reader.readAsDataURL(y);
        lsHinh.push(y);
      }

      lsHinh.forEach(function (el) {
        promise = promise.then(function () {
          socket.emit("SendMess", JSON.stringify(tinNhan), el);
          return new Promise(function (resolve) {
            setTimeout(resolve, interval);
          });
        });
      });

      promise.then(function () {
        console.log("Loop finished.");
      });
    }
    // Gửi một hình
    else if (count == 1) {
      tinNhan.slImage = 1;
      tinNhan.loai = "Image";
      var reader = new FileReader();
      reader.onloadend = function () {
        // socket.emit("TimBanBe", sender);
        socket.emit("SendMess", JSON.stringify(tinNhan), ourFile);
        document
          .getElementById("preview" + id)
          .setAttribute("src", reader.result);
        id++;
      };
      reader.readAsDataURL(ourFile);
      $("#chat").append(
        '<li class="me">' +
          '<div class="entete">' +
          "<h3>10:12AM, Today </h3>" +
          "<h2> &nbsp;" +
          tenNguoiGui +
          "</h2>" +
          '<span class="status blue"></span>' +
          "</div>" +
          '<div class="triangle"></div>' +
          '<img id="preview' +
          id +
          '" class="message">' +
          "</li>"
      );
    }
  }
  // Gửi TEXT
  else {
    tinNhan.slImage = 0;
    tinNhan.loai = "text";
    $("#chat").append(
      '<li class="me">' +
        '<div class="entete">' +
        "<h3>10:12AM, Today </h3>" +
        "<h2> &nbsp;" +
        tenNguoiGui +
        "</h2>" +
        '<span class="status blue"></span>' +
        "</div>" +
        '<div class="triangle"></div>' +
        '<div class="message" >' +
        txtMess +
        "</div>" +
        "</li>"
    );
    socket.emit("SendMess", JSON.stringify(tinNhan), undefined);
  }

  $("#chat").animate({ scrollTop: 20000000 }, "slow");
  $("#upload_input").val(null);
}

// Mở Form Tạo nhóm
function openForm() {
  document.getElementById("form-group").style.display = "block";
  $("#listUser_FormTaoNhom").html("");
  lsUser.forEach(function (i) {
    if (i.sdt == nguoiGui) {
      dataUser = JSON.stringify(i);
    }

    $("#listUser_FormTaoNhom").append(
      '<li style="list-style: none;display: flex;">' +
        '<input class="messageCheckbox" type="checkbox" value=' +
        JSON.stringify(i) +
        ">" +
        '<span class="checkmark"></span>' +
        '<img src="" alt="">' +
        "<h2>" +
        i.name +
        "</h2>" +
        "</li>"
    );
  });
}

// Xử lý Tạo nhóm
function TaoNhom() {
  var tenNhom = $("#nameGroup");
  var nhomTruong = nguoiGui;
  var listTinNhanNhom = [];
  var listTVNhom = [];

  listTVNhom.push(JSON.parse(dataUser));

  var inputElements = document.getElementsByClassName("messageCheckbox");
  for (var i = 0; inputElements[i]; i++) {
    if (inputElements[i].checked) {
      listTVNhom.push(JSON.parse(inputElements[i].value));
    }
  }

  var Nhom = new Object();
  Nhom.tenNhom = tenNhom.val();
  Nhom.nhomTruong = nhomTruong;
  Nhom.lsTinNhan = listTinNhanNhom;
  Nhom.lsUser = listTVNhom;

  socket.emit("TaoPhongChat", JSON.stringify(Nhom));
  document.getElementById("form-group").style.display = "none";

  $("#boxContent").html("");
  //yêu cầu lấy list bạn bè từ server
  socket.emit("GetLsBanBe", nguoiGui);
}

// Mở giao diện Profile
function Profile() {
  $("#Left-chat").hide();
  $("#Right-chat").hide();
  $("#Right-Profile").show(1000);
  $("#Group").hide();
  document.getElementById("Group").style.display = "none";
  document.getElementById("Right-chat").style.right = "10px";
}

// Mở giao diện Chat
function ChatMess() {
  $("#Left-chat").show(1000);
  $("#Right-chat").show(1000);
  $("#Right-Profile").hide();
  $("#boxContent").html("");
  //yêu cầu lấy list bạn bè từ server
  socket.emit("GetLsBanBe", nguoiGui);
  document.getElementById("Group").style.display = "none";
  document.getElementById("Right-chat").style.right = "10px";
}

// Mở Form thêm thành viên
function openFormAddToGroup() {
  $("#listUser_FormAddNhom").html("");
  document.getElementById("form-addToGroup").style.display = "block";
  var nguoivuathem = [];
  lsUser.forEach(function (i) {
    i.lsNhom.forEach(function (k) {
      if (k == tenNhom) {
        nguoivuathem.push(i.name);
      }
    });
  });

  lsUser.forEach(function (i) {
    if (check_arr(i.name, nguoivuathem) === false) {
      $("#listUser_FormAddNhom").append(
        '<li style="list-style: none;display: flex;">' +
          '<input class="messageCheckbox_AddUser" type="checkbox" value=' +
          JSON.stringify(i) +
          ">" +
          '<span class="checkmark"></span>' +
          '<img src="" alt="">' +
          "<h2>" +
          i.name +
          "</h2>" +
          "</li>"
      );
    }
  });
}

// Xử lý thêm thành viên
function addUsertoGroup() {
  var listTVNhom = [];

  var inputElements = document.getElementsByClassName(
    "messageCheckbox_AddUser"
  );
  for (var i = 0; inputElements[i]; i++) {
    if (inputElements[i].checked) {
      listTVNhom.push(JSON.parse(inputElements[i].value));
    }
  }

  listTVNhom.forEach(function (i) {
    socket.emit("ThemThanhVien", JSON.stringify(i), tenNhom);
  });
  document.getElementById("form-addToGroup").style.display = "none";
}

// Mở Form Xóa thành viên
function openFormDeleteToGroup() {
  document.getElementById("form-deleteToGroup").style.display = "block";
  $("#listUser_FormDeleteUser").html("");
  lsUser.forEach(function (i) {
    i.lsNhom.forEach(function (k) {
      if (k == tenNhom) {
        $("#listUser_FormDeleteUser").append(
          '<li style="list-style: none;display: flex;">' +
            '<input class="messageCheckbox_DeleteUser" type="checkbox" value=' +
            JSON.stringify(i) +
            ">" +
            '<span class="checkmark"></span>' +
            '<img src="" alt="">' +
            "<h2>" +
            i.name +
            "</h2>" +
            "</li>"
        );
      }
    });
  });
}

// Xử lý Xóa thành viên
function DeleteUserFromGroup() {
  var listTVNhom = [];

  var inputElements = document.getElementsByClassName(
    "messageCheckbox_DeleteUser"
  );
  for (var i = 0; inputElements[i]; i++) {
    if (inputElements[i].checked) {
      listTVNhom.push(JSON.parse(inputElements[i].value));
    }
  }

  listTVNhom.forEach(function (i) {
    socket.emit("XoaThanhVien", i.sdt, tenNhom);
  });
  document.getElementById("form-deleteToGroup").style.display = "none";
}

// Mở Form chuyển Quyền
function openAdminGroup() {
  document.getElementById("form-adminGroup").style.display = "block";
  $("#listUser_FormAdminNhom").html("");
  lsUser.forEach(function (i) {
    i.lsNhom.forEach(function (k) {
      if (k == tenNhom) {
        $("#listUser_FormAdminNhom").append(
          '<li style="list-style: none;display: flex;">' +
            '<input class="messageCheckbox_AdminUser" type="checkbox" value=' +
            JSON.stringify(i) +
            ">" +
            '<span class="checkmark"></span>' +
            '<img src="" alt="">' +
            "<h2>" +
            i.name +
            "</h2>" +
            "</li>"
        );
      }
    });
  });
}

// Xử lý chuyển Quyền
function chooseLeader() {
  var listTVNhom = [];

  var inputElements = document.getElementsByClassName(
    "messageCheckbox_AdminUser"
  );
  for (var i = 0; inputElements[i]; i++) {
    if (inputElements[i].checked) {
      listTVNhom.push(JSON.parse(inputElements[i].value));
    }
  }

  listTVNhom.forEach(function (i) {
    socket.emit("UyQuyenNhomTruong", i, tenNhom);
  });
  document.getElementById("form-adminGroup").style.display = "none";
}

// Đóng Form Tạo Nhóm
function closeForm() {
  document.getElementById("form-group").style.display = "none";
}

// Đóng Form Thêm Thành viên
function closeFormAddToGroup() {
  document.getElementById("form-addToGroup").style.display = "none";
}

// Đóng Form Xóa Thành viên
function closeFormDeleteToGroup() {
  document.getElementById("form-deleteToGroup").style.display = "none";
}

// Đóng Form Chuyển Quyền
function closeAdminGroup() {
  document.getElementById("form-adminGroup").style.display = "none";
}

// Mở Form  Profile
function openFormProfile() {
  document.getElementById("form-profile").style.display = "block";
}

// Đóng Form  Profile
function closeFormProfile() {
  document.getElementById("form-profile").style.display = "none";
}

// Đóng Giao diện thông tin nhóm
function closeGroup() {
  document.getElementById("Group").style.display = "none";
  document.getElementById("Right-chat").style.right = "10px";
}

// Mở Giao diện thông tin nhóm
function openGroup() {
  document.getElementById("Group").style.display = "block";
  document.getElementById("Right-chat").style.right = "232px";
  $("#divTenNhom").html("");
  $("#divTenNhom").append('<h2 id="h2TenNhom">' + tenNhom + "</h2>");
}

// Mở Xem thành viên
function openOutGroup() {
  document.getElementById("myDropdown").style.display = "block";
}

// Đóng Xem thành viên
function closeOutGroup() {
  document.getElementById("myDropdown").style.display = "none";
}

// Xử lý Xem thành viên
function myFunction() {
  document.getElementById("myDropdown").classList.toggle("show");
}

function myFunction1() {
  $("#myDropdown1").html("");
  document.getElementById("myDropdown1").classList.toggle("show1");
  lsUser.forEach(function (i) {
    i.lsNhom.forEach(function (k) {
      if (k == tenNhom) {
        $("#myDropdown1").append(
          '<li class="GroupUser" style="margin-top: 5px;margin-bottom: 5px;border: 1px solid whitesmoke;">' +
            '<img src="https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg" alt="">' +
            '<label for="" style="margin-top: 20px;">' +
            i.name +
            "</label>" +
            "</li>"
        );
      }
    });
  });
}

// Close the dropdown if the user clicks outside of it
//   window.onclick = function(event) {
//     if (!event.target.matches('.dropbtn')) {
//       var dropdowns = document.getElementsByClassName("dropdown-content");
//       var i;
//       for (i = 0; i < dropdowns.length; i++) {
//         var openDropdown = dropdowns[i];
//         if (openDropdown.classList.contains('show')) {
//           openDropdown.classList.remove('show');
//         }
//       }
//     }
//   }
