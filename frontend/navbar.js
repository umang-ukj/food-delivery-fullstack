function renderNavbar() {
  const authDiv = document.getElementById("navbarAuth");
  if (!authDiv) return;

  const token = localStorage.getItem("jwt");
  const email = localStorage.getItem("userEmail");

  if (token) {
    authDiv.innerHTML = `
       ${email}
      <button onclick="logout()">Logout</button>
    `;
  } else {
    authDiv.innerHTML = `
      <button onclick="goToLogin()">Login</button>
    `;
  }
}

function logout() {
  localStorage.clear();
  window.location.href = "index.html";
}

function goToLogin() {
  window.location.href = "login.html";
}
