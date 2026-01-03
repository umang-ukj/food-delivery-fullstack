function renderNavbar() {
  const token = localStorage.getItem("jwt");

  const emailSpan = document.getElementById("userEmail");
  const adminLink = document.getElementById("adminLink");
  const browseLink = document.getElementById("browseLink");
  const ordersLink = document.getElementById("ordersLink");
  //const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");

  if (!token) {
    // logged out state
    emailSpan.innerText = "";
    adminLink.style.display = "none";
    browseLink.style.display = "inline";
    ordersLink.style.display = "inline";
   // loginBtn.style.display = "inline";
    logoutBtn.style.display = "none";
    return;
  }

  const payload = parseJwt(token);
  const email = payload?.sub;
  const role = payload?.role;

  emailSpan.innerText = email;

  //loginBtn.style.display = "none";
  logoutBtn.style.display = "inline";

if (role === "admin") {
    // ADMIN rules
    adminLink.style.display = "inline";
    browseLink.style.display = "none";
    ordersLink.style.display = "none";
  } else {
    // USER rules
    adminLink.style.display = "none";
    browseLink.style.display = "inline";
    ordersLink.style.display = "inline";
  }
}

function logout() {
  localStorage.clear();
  window.location.href = "index.html";
}

function goToLogin() {
  window.location.href = "login.html";
}

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch {
    return null;
  }
}
