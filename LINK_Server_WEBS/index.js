const express = require("express");
const app = express();
const server = require("http").createServer(app);
const io = require("socket.io")(server);
var fs = require("fs");

app.use(express.static("./public"));
app.set("view engine", "ejs");
app.set("views", "./views");

const accountSid = "AC1ba556e19b38be6d32a41b14a20f90c2";
const authToken = "c2039423aa725476a49bc97578049ac2";
const client = require("twilio")(accountSid, authToken);

const AWS = require("aws-sdk");
const { Console } = require("console");
const config = new AWS.Config({
  accessKeyId: "AKIARVHNIIHQUROPKA7N",
  secretAccessKey: "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",
  region: "ap-southeast-1",
});
AWS.config = config;

const docClient = new AWS.DynamoDB.DocumentClient();

server.listen(3000);

var lsUserOnline = [];
var lsMess = [];
var sdtUserOnl = "";
var nameUserOnl = "";
var name = "";

//mở kết nối phía server nodeJS
io.sockets.on("connection", function (socket) {
  console.log("User In : " + socket.id);
  //Đăng nhập gửi vào 1 user---
  socket.on("DangNhap", function (data) {
    var user = JSON.parse(data);
    var tk = user.sdt;
    var mk = user.matKhau;
    var kq = false;
    var params = {
      TableName: "User",
      Key: { sdt: tk },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        socket.emit("ketQua", { noiDung: kq });
      } else {
        console.log("Success", data.Item);
        if (data.Item.matKhau == mk) {
          kq = true;
          sdtUserOnl = tk;
          nameUserOnl = data.Item.name;
        }
        socket.emit("ketQua", { noiDung: kq });
      }
    });
  });
  //Đăng ký tài khoản gửi vào 1 user---
  socket.on("DangKy", function (data) {
    console.log("Đăng ký ; data: " + data);
    var kq = true;
    var user = JSON.parse(data);
    var sdt = user.sdt;
    var matKhau = user.matKhau;
    var name = user.name;
    var avatar = "";
    var lsNhom = [];
    var kq = true;

    var params = {
      TableName: "User",
      Key: { sdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {

      } else {
        if(data.Item == undefined){
          const params = {
            TableName: "User",
            Item: {
              sdt,
              matKhau,
              name,
              avatar,
              lsNhom,
            },
          };
          docClient.put(params, function (err, data) {
            if (err) {
              console.log("Error", err);
            } else {
              console.log("Success", data);
              socket.emit("ketQua", { noiDung: kq });
            }
          });
        }else{
          kq = false
          socket.emit("ketQua", { noiDung: kq });
        }
      }
    });
  });
  //Gửi mã OTP vào sđt---
  socket.on("sendOTP", function (data) {
    var kq = true;
    console.log(data);
    code = JSON.parse(data);
    console.log(code.sdt);
    console.log(code.code);
    // client.messages
    //   .create({
    //     body: code.code,
    //     messagingServiceSid: 'MG3f3d1acf4703afcde2e7a3ed6ba32ee2',
    //     to: '+84' + code.sdt
    //   }).then(message => console.log(message.sid)).catch(error => kq = false)
    socket.emit("KQ", kq);
  });
  //Quên mật khẩu---
  socket.on("QuenMK", function (data) {
    console.log("Quên mật khẩu : data: " + data);
    var user = JSON.parse(data);
    var sdt = user.sdt;
    var matKhau = user.matKhau;
    // var name = user.name
    var kq = true;
    var params = {
      TableName: "User",
      Key: { sdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        socket.emit("KQCheckSDT", "SĐT chưa đc đăng ký");
      } else {
        var params = {
          TableName: "User",
          Key: {
            sdt,
          },
          UpdateExpression: "set matKhau = :t",
          ExpressionAttributeValues: {
            ":t": matKhau,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
            kq = false;
          } else {
            console.log("Success", data);
          }
          socket.emit("KetQua", { noiDung: kq });
        });
      }
    });
  });
  //Tìm bạn bè
  socket.on("TimBanBe", function (data) {
    var params = {
      TableName: "User",
      Key: {
        sdt: data,
      },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data.Item);
         socket.emit("kQTimBanBe", data.Item)
      }
    });
  });
  //ListBanBe
  socket.on("GetLsBanBe", function (mysdt) {
    var lsNguoiDung = [];
    var params = {
      TableName: "User",
    };
    docClient.scan(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        lsNhom = data.Items.lsNhom;
        lsNguoiDung = data.Items;
        lsNguoiDung.forEach(function (i) {
          socket.emit("slBanbe", lsNguoiDung.length -1);
          if (i.sdt == mysdt) {
            socket.emit("slNhom", i.lsNhom.length);
            lsNhom = i.lsNhom
            lsNhom.forEach(function(i){
              var params = {
                TableName: "NhomChat",
                Key: {
                  "tenNhom": i,
                },
              };
              docClient.get(params, function (err, data) {
                if (err) {
                  console.log("Error", err);
                } else {
                  console.log(data)
                  fs.readFile(data.Item.avatar + "", function (err, img) {
                    if (err) {
                      console.log(err);
                      socket.emit("lsNhom", data.Item);
                    } else {
                      socket.emit("lsNhom", data.Item, img);
                    }
                  });
                }
              });
            })
          }else{
            fs.readFile(i.avatar + "", function (err, img) {
              if (err) {
                console.log(err);
                socket.emit("lsBanBe", i);
              } else {
                console.log(i.avatar)
                socket.emit("lsBanBe", i, img);
              }
            });
          }
        });
      }
    });
  });
  //ListBanBe web
  socket.on("GetLsBanBeWeb", function (mysdt) {
    var lsNguoiDung = [];
    var params = {
      TableName: "User",
    };
    docClient.scan(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        lsNhom = data.Items.lsNhom;
        lsNguoiDung = data.Items;
        lsNguoiDung.forEach(function (i) {
          if (i.sdt == mysdt) {
            socket.emit("lsBanBeWeb", lsNguoiDung, i.lsNhom);
            return;
          }
        });
      }
    });
  });
  //Online
  socket.on("ImOnline", function (data) {
    lsUserOnline[data] = socket.id;
    console.log(lsUserOnline);
  });
  //Send mess
  socket.on("SendMess", function (data, Image) {
    var TinNhan = JSON.parse(data);
    var nguoiGui = TinNhan.nguoiGui;
    var nguoiNhan = TinNhan.nguoiNhan;
    var noiDung = TinNhan.noiDung;
    var loai = TinNhan.loai;
    var sl = TinNhan.slImage;
    var thoiGianGui = TinNhan.thoiGianGui;
    var viTriPhanHoi = TinNhan.viTriPhanHoi;
    var nameImage = "";
    if (Image != undefined) {
      nameImage = saveImage(Image, socket.id);
      TinNhan.noiDung = nameImage;
    }
    if (nguoiNhan.indexOf("NhomChat") != -1) {
      //nhắn tin nhóm

      if (loai.indexOf("lsImage") != -1) {
        if (nameImage != "") {
          noiDung = nameImage;
        }
        fs.readFile(noiDung + "", function (err, data) {
          if (err) {
            console.log(err);
          } else {
            var nhomNhan = nguoiNhan.replace("NhomChat", "");
            socket.to(nhomNhan).emit("getMess", TinNhan, data);
            SaveMessageGroup_lsImage(
              nhomNhan,
              nguoiGui,
              nguoiNhan,
              noiDung,
              loai,
              sl,
              thoiGianGui,
              viTriPhanHoi
            );
          }
        });
      } else if (loai.indexOf("Image") != -1 || loai.indexOf("Voice") != -1) {
        if (nameImage != "") {
          noiDung = nameImage;
        }
        fs.readFile(noiDung + "", function (err, data) {
          if (err) {
            console.log(err);
          } else {
            var nhomNhan = nguoiNhan.replace("NhomChat", "");
            socket.to(nhomNhan).emit("getMess", TinNhan, data);
            SaveMessageGroup(
              nhomNhan,
              nguoiGui,
              nguoiNhan,
              noiDung,
              loai,
              sl,
              thoiGianGui,
              viTriPhanHoi
            );
          }
        });
      } else {
        var nhomNhan = nguoiNhan.replace("NhomChat", "");
        socket.to(nhomNhan).emit("getMess", TinNhan);
        SaveMessageGroup(
          nhomNhan,
          nguoiGui,
          nguoiNhan,
          noiDung,
          loai,
          sl,
          thoiGianGui,
          viTriPhanHoi
        );
      }
    } else {
      //nhắn tin đôi
      if (loai.indexOf("lsImage") != -1) {
        if (nameImage != "") {
          noiDung = nameImage;
        }
        fs.readFile(noiDung + "", function (err, data) {
          if (err) {
            console.log(err);
          } else {
            io.to(lsUserOnline[TinNhan.nguoiNhan]).emit(
              "getMess",
              TinNhan,
              data
            );
            saveMessage_lsImage(
              nguoiGui,
              nguoiNhan,
              noiDung,
              loai,
              sl,
              thoiGianGui,
              viTriPhanHoi
            );
          }
        });
      } else if (loai.indexOf("Image") != -1 || loai.indexOf("Voice") != -1) {
        if (nameImage != "") {
          noiDung = nameImage;
        }
        fs.readFile(noiDung + "", function (err, data) {
          if (err) {
            console.log(err);
          } else {
            io.to(lsUserOnline[TinNhan.nguoiNhan]).emit(
              "getMess",
              TinNhan,
              data
            );
            saveMessage(
              nguoiGui,
              nguoiNhan,
              noiDung,
              loai,
              sl,
              thoiGianGui,
              viTriPhanHoi
            );
          }
        });
      } else {
        io.to(lsUserOnline[TinNhan.nguoiNhan]).emit("getMess", TinNhan);
        saveMessage(
          nguoiGui,
          nguoiNhan,
          noiDung,
          loai,
          sl,
          thoiGianGui,
          viTriPhanHoi
        );
      }
    }
  });
  //Load mess
  socket.on("loadMess", function (data) {
    lsMess = [];
    var tinNhan = JSON.parse(data);
    var nguoiGui = tinNhan.nguoiGui;
    var nguoiNhan = tinNhan.nguoiNhan;
    var params = {
      TableName: "Message",
      Key: {
        nguoiGui,
        nguoiNhan,
      },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        if (data.Item != undefined) {
          var lsTinNhan = data.Item.lsTinNhan;
          var slImage = 0;
          lsTinNhan.forEach(function (i) {
            if (i.slImage >= 2) {
              slImage += i.slImage;
            }
          });
          socket.emit("soLuongTinNhan", lsTinNhan.length, slImage);
          for (let i = 0; i < lsTinNhan.length; i++) {
            if (lsTinNhan[i].slImage == 0) {
              //text
              socket.emit("lsMess", lsTinNhan[i], i);
            } else if (lsTinNhan[i].slImage == 1) {
              //một hình
              fs.readFile(lsTinNhan[i].noiDung + "", function (err, data) {
                if (err) {
                  console.log(err);
                } else socket.emit("lsMess", lsTinNhan[i], i, data);
              });
            } else {
              //nhiều hình
              var lsImage = lsTinNhan[i].nameImageLs;
              for (let j = 0; j < lsImage.length; j++) {
                fs.readFile(lsImage[j] + "", function (err, data) {
                  if (err) console.log(err);
                  else {
                    lsTinNhan[i].noiDung = lsImage[j];
                    socket.emit("lsMess", lsTinNhan[i], i, data);
                  }
                });
              }
            }
          }
        }
      }
    });
  });
  //Load mess group
  socket.on("loadMessGroup", function (data) {
    lsMess = [];
    var tinNhan = JSON.parse(data);
    var nguoiGui = tinNhan.nguoiGui;
    var nhomChat = tinNhan.nguoiNhan;

    var params = {
      TableName: "NhomChat",
      Key: {
        tenNhom: nhomChat,
      },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        if (data.Item != undefined) {
          var lsTinNhan = data.Item.lsTinNhan;
          var slImage = 0;
          lsTinNhan.forEach(function (i) {
            if (i.slImage >= 2) {
              slImage += i.slImage;
            }
          });
          socket.emit("soLuongTinNhan", lsTinNhan.length, slImage);
          for (let i = 0; i < lsTinNhan.length; i++) {
            if (lsTinNhan[i].slImage == 0) {
              //text
              socket.emit("lsMess", lsTinNhan[i], i);
            } else if (lsTinNhan[i].slImage == 1) {
              //một hình
              fs.readFile(lsTinNhan[i].noiDung + "", function (err, data) {
                if (err) {
                  console.log(err);
                } else socket.emit("lsMess", lsTinNhan[i], i, data);
              });
            } else {
              //nhiều hình
              var lsImage = lsTinNhan[i].nameImageLs;
              for (let j = 0; j < lsImage.length; j++) {
                fs.readFile(lsImage[j] + "", function (err, data) {
                  if (err) console.log(err);
                  else {
                    lsTinNhan[i].noiDung = lsImage[j];
                    socket.emit("lsMess", lsTinNhan[i], i, data);
                  }
                });
              }
            }
          }
        }
      }
    });
  });
  //Tạo phòng chát nhóm
  socket.on("TaoPhongChat", function (data) {
    console.log("TaoPhongChat" + data);
    var Nhom = JSON.parse(data);
    var tenNhom = Nhom.tenNhom;
    var nhomTruong = Nhom.nhomTruong;
    var lsTinNhan = [];
    var avatar = "";
    lsTinNhan = Nhom.lsTinNhan;
    var lsUser = [];
    lsUser = Nhom.lsUser;
    var params = {
      TableName: "NhomChat",
      Item: {
        tenNhom,
        avatar,
        nhomTruong,
        lsTinNhan,
        lsUser,
      },
    }; //Thêm nhóm vào database
    docClient.put(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data);
        lsUser.forEach(function (i) {
          //vòng for làm từng user
          var params = {
            TableName: "User",
            Key: {
              sdt: i.sdt,
            },
          }; //lấy user ra để cập nhập list nhóm
          docClient.get(params, function (err, data) {
            if (err) {
              console.log("Error", err);
            } else {
              console.log("Success", data.Item);
              var lsNhom = [];
              if (data.Item.lsNhom != undefined) {
                lsNhom = data.Item.lsNhom;
              }
              if (lsNhom == null) {
                // list nhóm rỗng thì thêm nhóm đó vào
                lsNhom.push(tenNhom);

                var params = {
                  TableName: "User",
                  Key: {
                    sdt: i.sdt,
                  },
                  UpdateExpression: "set lsNhom = :t",
                  ExpressionAttributeValues: {
                    ":t": lsNhom,
                  },
                };
                docClient.update(params, function (err, data) {
                  if (err) {
                    console.log("Error", err);
                  } else {
                    console.log("Success", data);
                    io.to(lsUserOnline[i.sdt]).emit("VaoNhom", tenNhom);
                  }
                });
              } else if (!lsNhom.includes(tenNhom)) {
                //nếu ko rỗng phải xét xem nhóm đó đã có  chưa nếu có rồi thì ko thêm vào
                lsNhom.push(tenNhom);
                var params = {
                  TableName: "User",
                  Key: {
                    sdt: i.sdt,
                  },
                  UpdateExpression: "set lsNhom = :t",
                  ExpressionAttributeValues: {
                    ":t": lsNhom,
                  },
                };
                docClient.update(params, function (err, data) {
                  if (err) {
                    console.log("Error", err);
                  } else {
                    console.log("Success", data);
                    io.to(lsUserOnline[i.sdt]).emit("VaoNhom", tenNhom);
                  }
                });
              }
            }
          });
        });
      }
    });
  });
  //join phong chat
  socket.on("joinNhom", function (data) {
    console.log("join nhom");
    socket.join(data);
    var params = {
      TableName: "NhomChat",
      Key: {
        tenNhom: data,
      },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        socket.emit("ThongTinNhom", data.Item, data.Item.lsUser);
      }
    });
  });
  //Xóa thành viên trong nhóm
  socket.on("XoaThanhVien", function (sdt, tenNhom) {
    console.log("xóa thành viên : " + sdt + tenNhom);
    //Xóa thành viên trong table NhomChat
    var params = {
      TableName: "NhomChat",
      Key: { tenNhom: tenNhom },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        var lsUser = data.Item.lsUser.filter(function (i) {
          return i.sdt != sdt;
        });
        var params = {
          TableName: "NhomChat",
          Key: { tenNhom: tenNhom },
          UpdateExpression: "set lsUser = :t",
          ExpressionAttributeValues: {
            ":t": lsUser,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("Success", data);
          }
        });
      }
    });
    //Xóa lsNhom trong table User
    var params = {
      TableName: "User",
      Key: { sdt: sdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data.Item);
        var lsNhom = data.Item.lsNhom.filter(function (i) {
          return i != tenNhom;
        });
        var params = {
          TableName: "User",
          Key: { sdt: sdt },
          UpdateExpression: "set lsNhom = :t",
          ExpressionAttributeValues: {
            ":t": lsNhom,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("Success", data);
            socket.emit("Success");
          }
        });
      }
    });
  });
  //Thêm thành viên vào nhóm
  socket.on("ThemThanhVien", function (data, tenNhom) {
    var user = JSON.parse(data);
    var sdt = user.sdt;
    //thêm thành viên vào trong table NhomChat
    var params = {
      TableName: "NhomChat",
      Key: { tenNhom: tenNhom },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        var lsUser = data.Item.lsUser;
        lsUser.push(user);
        var params = {
          TableName: "NhomChat",
          Key: { tenNhom: tenNhom },
          UpdateExpression: "set lsUser = :t",
          ExpressionAttributeValues: {
            ":t": lsUser,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("Success", data);
          }
        });
      }
    });
    //Thêm thành viên vào tableUser
    var params = {
      TableName: "User",
      Key: { sdt: sdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data.Item);
        var lsNhom = data.Item.lsNhom;
        if (lsNhom == null) {
          lsNhom = [tenNhom];
        } else {
          lsNhom.push(tenNhom);
        }
        var params = {
          TableName: "User",
          Key: { sdt: sdt },
          UpdateExpression: "set lsNhom = :t",
          ExpressionAttributeValues: {
            ":t": lsNhom,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("Success", data);
            io.to(lsUserOnline[sdt]).emit("VaoNhom", tenNhom);
          }
        });
      }
    });
  });
  //Ủy quyền nhóm trưởng mới
  socket.on("UyQuyenNhomTruong", function (nhomtruongMoi, tenNhom) {
    var params = {
      TableName: "NhomChat",
      Key: {
        tenNhom: tenNhom,
      },
      UpdateExpression: "set nhomTruong = :t",
      ExpressionAttributeValues: {
        ":t": nhomtruongMoi,
      },
    };
    docClient.update(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data);
      }
    });
  });
  //Gỡ tin nhắn
  socket.on("GoTinNhan", function (nguoiGui, nguoiNhan, index) {
    if (nguoiNhan.indexOf("NhomChat") != -1) {//Chát nhóm
      var nhomNhan = nguoiNhan.replace("NhomChat", "");
      var params = {
        TableName: "NhomChat",
        Key: {
          tenNhom: nhomNhan,
        },
        UpdateExpression: "set lsTinNhan[" + index + "].loai =:tasksVal",
        ExpressionAttributeValues: {
          ":tasksVal": "TinNhanGo",
        },
        ReturnValues: "UPDATED_NEW",
      };
      docClient.update(params, function (err, data) {
        if (err) {
          console.error(err);
        } else {
          console.log("Success");
          socket.to(nhomNhan).emit("GoTin");
        }
      });
    } else { //Chat đơn
      var params = {
        TableName: "Message",
        Key: {
          nguoiGui,
          nguoiNhan,
        },
        UpdateExpression: "set lsTinNhan[" + index + "].loai =:tasksVal",
        ExpressionAttributeValues: {
          ":tasksVal": "TinNhanGo",
        },
        ReturnValues: "UPDATED_NEW",
      };
      docClient.update(params, function (err, data) {
        if (err) {
          console.error(err);
        } else {
          var params = {
            TableName: "Message",
            Key: {
              "nguoiGui" : nguoiNhan,
              "nguoiNhan" : nguoiGui,
            },
            UpdateExpression: "set lsTinNhan[" + index + "].loai =:tasksVal",
            ExpressionAttributeValues: {
              ":tasksVal": "TinNhanGo",
            },
            ReturnValues: "UPDATED_NEW",
          };
          docClient.update(params, function (err, data) {
            if (err) {
              console.error(err);
            } else {
              console.log("Success");
              socket.to(lsUserOnline[nguoiNhan]).emit("GoTin")
            }
          });
        }
      });
    }
  });
  //Thông tin user
  socket.on("getThongTinUser", function (mysdt) {
    var params = {
      TableName: "User",
      Key: { sdt: mysdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        var avatar = data.Item.avatar;
        fs.readFile(avatar + "", function (err, img) {
          if (err) {
            console.log(err);
            socket.emit("profile", data.Item, "");
          } else {
            socket.emit("profile", data.Item, img);
          }
        });
      }
    });
  });
  //Lưu thông tin user
  socket.on("saveImgProfile", function (img, sdt) {
    var avatar = saveImage(img, socket.id);
    var params = {
      TableName: "User",
      Key: {
        sdt,
      },
      UpdateExpression: "set avatar =:tasksVal, name=:t",
      ExpressionAttributeValues: {
        ":tasksVal": avatar,
      },
      ReturnValues: "UPDATED_NEW",
    };
    docClient.update(params, function (err, data) {
      if (err) {
        console.error(err);
      } else {
        console.log("Success");
      }
    });
  });
  //Giải tán nhóm
  socket.on("GiaiTanNhom", function (tenNhom) {
    var lsThanhVien = [];
    var params = {
      Key: {
        tenNhom,
      },
      TableName: "NhomChat",
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        lsThanhVien = data.Item.lsUser;
        //Xóa table NhomChat
        docClient.delete(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("Success", data.Item);
            //Xoa lsNhom trong mỗi thành viên
            var dem = 0;
            lsThanhVien.forEach(function (i) {
              var lsNhom = [];
              var sdt = i.sdt;
              var params = {
                TableName: "User",
                Key: {
                  sdt,
                },
              };
              //lay ls nhóm của từng thành viên
              docClient.get(params, function (err, data) {
                if (err) {
                  console.log("Error", err);
                } else {
                  lsNhom = data.Item.lsNhom;
                  const index = lsNhom.indexOf(tenNhom);
                  if (index > -1) {
                    // only splice array when item is found
                    lsNhom.splice(index, 1); // 2nd parameter means remove one item only
                    //Xóa xong update lại lsNhom trong table User
                    var params = {
                      TableName: "User",
                      Key: {
                        sdt,
                      },
                      UpdateExpression: "set lsNhom =:tasksVal",
                      ExpressionAttributeValues: {
                        ":tasksVal": lsNhom,
                      },
                    };
                    docClient.update(params, function (err, data) {
                      if (err) {
                        console.error(err);
                      } else {
                        console.log("Success");
                        dem++;
                        if (dem == lsThanhVien.length) {
                          console.log(dem + "/" + lsThanhVien.length);
                          socket.emit("ketQua", "oke");
                        }
                      }
                    });
                  }
                }
              });
            });
          }
        });
      }
    });
  });
  //xóa đoạn chát
  socket.on("XoaDoanChat", function (nguoiGui, nguoiNhan) {
    var listTinNhan = [];
    var params = {
      TableName: "Message",
      Key: {
        nguoiGui,
        nguoiNhan,
      },
      UpdateExpression: "set lsTinNhan = :t",
      ExpressionAttributeValues: {
        ":t": listTinNhan,
      },
    };
    docClient.update(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data);
        socket.emit("ketQua", "oke");
      }
    });
  });
  //Thông tin user
  socket.on("getThongTinUser", function (mysdt) {
    var params = {
      TableName: "User",
      Key: { sdt: mysdt },
    };
    docClient.get(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        var avatar = data.Item.avatar;
        fs.readFile(avatar + "", function (err, img) {
          if (err) {
            console.log(err);
            socket.emit("profile", data.Item, "");
          } else {
            socket.emit("profile", data.Item, img);
          }
        });
      }
    });
  });
  //Lưu thông tin user
  socket.on("saveImgProfile", function (img, sdt) {
    var avatar = saveImage(img, socket.id);
    var params = {
      TableName: "User",
      Key: {
        sdt,
      },
      UpdateExpression: "set avatar =:tasksVal",
      ExpressionAttributeValues: {
        ":tasksVal": avatar,
      },
      ReturnValues: "UPDATED_NEW",
    };
    docClient.update(params, function (err, data) {
      if (err) {
        console.error(err);
      } else {
        console.log("Success");
      }
    });
  });
  //Cap nhat profile
  socket.on("updateProfile", function (nameUser, sdt) {
    var params = {
      TableName: "User",
      Key: {
        sdt,
      },
      UpdateExpression: "set #n = :t",
      ExpressionAttributeValues: {
        ":t": nameUser,
      },
      ExpressionAttributeNames: {
        "#n": "name",
      },
      // ReturnValues: "UPDATED_NEW"
    };
    docClient.update(params, function (err, data) {
      if (err) {
        console.log("Error", err);
      } else {
        console.log("Success", data);
      }
    });
  });
});

//Lưu hình ảnh vào server
function saveImage(Image, id) {
  name = getFileImageName(id);
  fs.writeFileSync(name, Image);
  return name;
}

function getFileImageName(id) {
  var date = new Date();
  var milis = date.getTime();
  return "imageUser/" + id.substring(2) + milis + ".jpg";
}

function saveMessage(nguoiGui,nguoiNhan,noiDung,loai,sl,thoiGianGui,viTriPhanHoi) {
  var TinNhan = new Object();
  TinNhan.nguoiGui = nguoiGui;
  TinNhan.nguoiNhan = nguoiNhan;
  TinNhan.noiDung = noiDung;
  TinNhan.loai = loai;
  TinNhan.slImage = sl;
  TinNhan.thoiGianGui = thoiGianGui;
  TinNhan.viTriPhanHoi = viTriPhanHoi;

  var lsTinNhan = [];
  lsTinNhan.push(TinNhan);

  var params = {
    TableName: "Message",
    Key: {
      nguoiGui,
      nguoiNhan,
    },
  };
  docClient.get(params, function (err, data) {
    if (err) {
      console.log("Error", err);
    } else {
      if (data.Item == undefined) {
        //nếu chưa có tin nhắn thì thêm mới
        var params = {
          TableName: "Message",
          Item: {
            nguoiGui,
            nguoiNhan,
            lsTinNhan,
          },
        };
        docClient.put(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            var params = {
              TableName: "Message",
              Item: {
                nguoiGui: nguoiNhan,
                nguoiNhan: nguoiGui,
                lsTinNhan,
              },
            };
            docClient.put(params, function (err, data) {
              if (err) {
                console.log("Error", err);
              } else {
                console.log("Success", data);
              }
            });
          }
        });
      } else {
        // nếu có rồi thì update lsTinNhan
        var params = {
          TableName: "Message",
          Key: {
            nguoiGui,
            nguoiNhan,
          },
          UpdateExpression: "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
          ExpressionAttributeValues: {
            ":newTask": lsTinNhan,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            var params = {
              TableName: "Message",
              Key: {
                nguoiGui: nguoiNhan,
                nguoiNhan: nguoiGui,
              },
              UpdateExpression:
                "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
              ExpressionAttributeValues: {
                ":newTask": lsTinNhan,
              },
            };
            docClient.update(params, function (err, data) {
              if (err) {
                console.log("Error", err);
              } else {
                console.log("Success", data);
              }
            });
          }
        });
      }
    }
  });
}

function saveMessage_lsImage(nguoiGui,nguoiNhan,noiDung,loai,sl,thoiGianGui,viTriPhanHoi) {
  var nameImageLs = [];
  nameImageLs.push(noiDung);

  var TinNhan = new Object();
  TinNhan.nguoiGui = nguoiGui;
  TinNhan.nguoiNhan = nguoiNhan;
  TinNhan.noiDung = noiDung;
  TinNhan.loai = loai;
  TinNhan.slImage = sl;
  TinNhan.nameImageLs = nameImageLs;
  TinNhan.thoiGianGui = thoiGianGui;
  TinNhan.viTriPhanHoi = viTriPhanHoi;

  console.log(TinNhan);

  var lsTinNhan = [];
  lsTinNhan.push(TinNhan);

  var params = {
    TableName: "Message",
    Key: {
      nguoiGui,
      nguoiNhan,
    },
  };
  docClient.get(params, function (err, data) {
    if (err) {
      console.log("Error", err);
    } else {
      console.log(data.Item);
      if (data.Item == undefined) {
        //nếu chưa có tin nhắn thì thêm mới
        console.log("Vô chưa có tin nhắn thì thêm mới");
        var params = {
          TableName: "Message",
          Item: {
            nguoiGui,
            nguoiNhan,
            lsTinNhan,
          },
        };
        docClient.put(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            var params = {
              TableName: "Message",
              Item: {
                nguoiGui: nguoiNhan,
                nguoiNhan: nguoiGui,
                lsTinNhan,
              },
            };
            docClient.put(params, function (err, data) {
              if (err) {
                console.log("Error", err);
              } else {
                console.log("Success", data);
              }
            });
          }
        });
      } else {
        // nếu có rồi thì update lsTinNhan
        console.log("Vô có tin nhắn rồi");
        var listTinNhan2 = data.Item.lsTinNhan;
        var lsImageName = [];
        var dem = 0;

        for (let i = listTinNhan2.length - 1; i >= 0; i--) {
          if (listTinNhan2[i].thoiGianGui + "" == thoiGianGui + "") {
            console.log("Gộp");
            dem++;
            lsImageName = listTinNhan2[i].nameImageLs;
            lsImageName.push(noiDung);
            listTinNhan2[i].nameImageLs = lsImageName;
            var params = {
              TableName: "Message",
              Key: {
                nguoiGui,
                nguoiNhan,
              },
              UpdateExpression: "set lsTinNhan = :t",
              ExpressionAttributeValues: {
                ":t": listTinNhan2,
              },
            };
            docClient.update(params, function (err, data) {
              if (err) {
                console.log("Error", err);
              } else {
                var params = {
                  TableName: "Message",
                  Key: {
                    nguoiGui: nguoiNhan,
                    nguoiNhan: nguoiGui,
                  },
                  UpdateExpression: "set lsTinNhan = :t",
                  ExpressionAttributeValues: {
                    ":t": listTinNhan2,
                  },
                };
                docClient.update(params, function (err, data) {
                  if (err) {
                    console.log("Error", err);
                  } else {
                    console.log("Success", data);
                    return;
                  }
                });
              }
            });
          }
        }
        if (dem == 0) {
          console.log("Không gộp tự tạo mới");
          var params = {
            TableName: "Message",
            Key: {
              nguoiGui,
              nguoiNhan,
            },
            UpdateExpression:
              "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
            ExpressionAttributeValues: {
              ":newTask": lsTinNhan,
            },
          };
          docClient.update(params, function (err, data) {
            if (err) {
              console.log("Error", err);
            } else {
              var params = {
                TableName: "Message",
                Key: {
                  nguoiGui: nguoiNhan,
                  nguoiNhan: nguoiGui,
                },
                UpdateExpression:
                  "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
                ExpressionAttributeValues: {
                  ":newTask": lsTinNhan,
                },
              };
              docClient.update(params, function (err, data) {
                if (err) {
                  console.log("Error", err);
                } else {
                  console.log("Success", data);
                }
              });
            }
          });
        }
      }
    }
  });
}

function SaveMessageGroup(nhomNhan,nguoiGui,nguoiNhan,noiDung,loai,sl,thoiGianGui,viTriPhanHoi) {
  var TinNhan = new Object();
  TinNhan.nguoiGui = nguoiGui;
  TinNhan.nguoiNhan = nhomNhan;
  TinNhan.noiDung = noiDung;
  TinNhan.loai = loai;
  TinNhan.slImage = sl;
  TinNhan.thoiGianGui = thoiGianGui;
  TinNhan.viTriPhanHoi = viTriPhanHoi;

  console.log(TinNhan);

  var lsTinNhan = [];
  lsTinNhan.push(TinNhan);

  var params = {
    TableName: "NhomChat",
    Key: {
      tenNhom: nhomNhan,
    },
    UpdateExpression: "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
    ExpressionAttributeValues: {
      ":newTask": lsTinNhan,
    },
  };
  docClient.update(params, function (err, data) {
    if (err) {
      console.log("Error", err);
    } else {
      console.log("Success", data);
    }
  });
}

function SaveMessageGroup_lsImage(nhomNhan,nguoiGui,nguoiNhan,noiDung,loai,sl,thoiGianGui,viTriPhanHoi) {
  var nameImageLs = [];
  nameImageLs.push(noiDung);

  var TinNhan = new Object();
  TinNhan.nguoiGui = nguoiGui;
  TinNhan.nguoiNhan = nhomNhan;
  TinNhan.noiDung = noiDung;
  TinNhan.loai = loai;
  TinNhan.slImage = sl;
  TinNhan.nameImageLs = nameImageLs;
  TinNhan.thoiGianGui = thoiGianGui;
  TinNhan.viTriPhanHoi = viTriPhanHoi;

  var lsTinNhan = [];
  lsTinNhan.push(TinNhan);

  var params = {
    TableName: "NhomChat",
    Key: {
      tenNhom: nhomNhan,
    },
  };
  docClient.get(params, function (err, data) {
    if (err) {
      console.log("Error", err);
    } else {
      var listTinNhan2 = data.Item.lsTinNhan;
      var lsImageName = [];
      var dem = 0;
      for (let i = 0; i < listTinNhan2.length; i++) {
        if (listTinNhan2[i].thoiGianGui == thoiGianGui) {
          dem++;
          lsImageName = listTinNhan2[i].nameImageLs;
          lsImageName.push(noiDung);
          listTinNhan2[i].nameImageLs = lsImageName;
          var params = {
            TableName: "NhomChat",
            Key: {
              tenNhom: nhomNhan,
            },
            UpdateExpression: "set lsTinNhan = :t",
            ExpressionAttributeValues: {
              ":t": listTinNhan2,
            },
          };
          docClient.update(params, function (err, data) {
            if (err) {
              console.log("Error", err);
            } else {
              console.log("oke");
            }
          });
        }
      }
      if (dem == 0) {
        var params = {
          TableName: "NhomChat",
          Key: {
            tenNhom: nhomNhan,
          },
          UpdateExpression: "SET lsTinNhan = list_append(lsTinNhan, :newTask)",
          ExpressionAttributeValues: {
            ":newTask": lsTinNhan,
          },
        };
        docClient.update(params, function (err, data) {
          if (err) {
            console.log("Error", err);
          } else {
            console.log("ok");
          }
        });
      }
    }
  });
}

app.get("/", (req, res) => {
  res.render("DangNhap");
});

app.get("/OPDangKy", (req, res) => {
  res.render("DangKy");
});

app.get("/QuenMatKhau", (req, res) => {
  res.render("QuenMatKhau");
});

app.get("/OPChat", (req, res) => {
  res.render("chat", {
    data: sdtUserOnl,
    name: nameUserOnl,
  });
});
