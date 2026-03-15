function renderNavbar() {
  const token = localStorage.getItem("jwt");
  const emailSpan = document.getElementById("userEmail");
  const adminLink = document.getElementById("adminLink");
  const browseLink = document.getElementById("browseLink");
  const ordersLink = document.getElementById("ordersLink");
  //const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const cartLink = document.getElementById("cartLink");

  if (!token) {
    // logged out state
    emailSpan.innerText = "";
    adminLink.style.display = "none";
    browseLink.style.display = "inline";
    ordersLink.style.display = "inline";
   // loginBtn.style.display = "inline";
    logoutBtn.style.display = "none";
    cartLink.style.display = "none";
    return;
  }

  const payload = parseJwt(token);
  const email = payload?.sub;
  const role = payload?.role;

  if (role !== "admin") {
  document.getElementById("addressLink").style.display = "inline";
}

  emailSpan.innerText = email;

  //loginBtn.style.display = "none";
  logoutBtn.style.display = "inline";

if (role === "admin") {
    // ADMIN rules
    adminLink.style.display = "inline";
    browseLink.style.display = "none";
    ordersLink.style.display = "none";
    cartLink.style.display = "none";
  } else {
    // USER rules
    adminLink.style.display = "none";
    browseLink.style.display = "inline";
    ordersLink.style.display = "inline";
    setupCartLink(cartLink);
  }
  setupBrandClick();
}
function setupCartLink(cartLink) {
  if (!cartLink) return;

  const savedCartRaw = localStorage.getItem("fd_cart");
  if (!savedCartRaw) {
    cartLink.style.display = "none";
    cartLink.onclick = null;
    return;
  }

  try {
    const savedCart = JSON.parse(savedCartRaw);
    const hasItems = Array.isArray(savedCart.items) && savedCart.items.length > 0;
    const restaurantId = savedCart.restaurantId;

    if (!hasItems || !restaurantId) {
      cartLink.style.display = "none";
      cartLink.onclick = null;
      return;
    }

    cartLink.style.display = "inline";
    cartLink.onclick = (e) => {
      e.preventDefault();
      localStorage.setItem("selectedRestaurantId", restaurantId);
      window.location.href = "menu.html";
    };
  } catch {
    cartLink.style.display = "none";
    cartLink.onclick = null;
  }
}

function setupBrandClick() {
  const brand = document.getElementById("brandLink");
  if (!brand) return;

  const token = localStorage.getItem("jwt");

  //  Not logged in → disabled
  if (!token) {
    brand.style.cursor = "default";
    brand.onclick = null;
    return;
  }

  const payload = JSON.parse(atob(token.split(".")[1]));
  const role = payload.role;

  //  Admin → disabled (IMPORTANT)
  if (role === "admin") {
    brand.style.cursor = "default";
    brand.onclick = null;
    return;
  }

  //  Normal user → go to restaurants
  brand.style.cursor = "pointer";
  brand.onclick = () => {
    window.location.href = "restaurants.html";
  };
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
