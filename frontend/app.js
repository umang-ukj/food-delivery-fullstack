let token = "";
const API_BASE = "http://localhost:8080";
let selectedRestaurantId = null;
let selectedRestaurantName = null;
let selectedRestaurantImageUrl = null;
//let selectedItems = [];
let cart = [];
let cartRestaurantId = null;
let cartRestaurantName = null;
let cartRestaurantImageUrl = null;
let editingAddressId = null;
let searchTimeout = null;
let currentRestaurantLocation = null;
let restaurantDietFilter = "all";
let menuDietFilter = "all";
const CART_STORAGE_KEY = "fd_cart";

function loadCartStateFromStorage() {
  const raw = localStorage.getItem(CART_STORAGE_KEY);

  if (!raw) {
    cart = [];
    cartRestaurantId = null;
    cartRestaurantName = null;
    cartRestaurantImageUrl = null;
    return;
  }

  try {
    const parsed = JSON.parse(raw);
    cart = Array.isArray(parsed.items) ? parsed.items : [];
    cartRestaurantId = parsed.restaurantId || null;
    cartRestaurantName = parsed.restaurantName || null;
    cartRestaurantImageUrl = parsed.restaurantImageUrl || null;
  } catch {
    cart = [];
    cartRestaurantId = null;
    cartRestaurantName = null;
    cartRestaurantImageUrl = null;
    localStorage.removeItem(CART_STORAGE_KEY);
  }
}

function saveCartState() {
  const payload = {
    restaurantId: cartRestaurantId,
    restaurantName: cartRestaurantName,
    restaurantImageUrl: cartRestaurantImageUrl,
    items: cart
  };

  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(payload));
}

function clearCartState() {
  cart = [];
  cartRestaurantId = null;
  cartRestaurantName = null;
  cartRestaurantImageUrl = null;
  localStorage.removeItem(CART_STORAGE_KEY);
}

function getUserRole() {
  const token = localStorage.getItem("jwt");
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.role;
  } catch {
    return null;
  }
}

function login() {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  const errorEl = document.getElementById("loginError");
  const loginBtn = document.getElementById("loginBtn");

  errorEl.style.display = "none";

  // Basic validation
  if (!email || !password) {
    showError("Email and password are required");
    return;
  }

  if (!email.includes("@")) {
    showError("Invalid email format");
    return;
  }

  // Loading state
  loginBtn.disabled = true;
  loginBtn.innerText = "Logging in...";

  fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  })
    .then(res => {
      if (!res.ok) throw new Error("Invalid email or password");
      return res.json();
    })
    .then(data => {
      // Store auth info
      localStorage.setItem("jwt", data.token);
      localStorage.setItem("userEmail", email);

      const payload = JSON.parse(atob(data.token.split(".")[1]));
      const role = payload.role;

      const params = new URLSearchParams(window.location.search);
      const nextPage = params.get("next") || "restaurants.html";
      const shouldClosePopup = params.get("popup") === "1";

      // Redirect by role
      if (role === "admin") {
        window.location.href = "admin.html";
      }
      else if (shouldClosePopup && window.opener) {
        window.opener.postMessage({ type: "fd_login_success" }, window.location.origin);
        window.close();
      } else {
        //window.location.href = "restaurants.html";
        window.location.href = nextPage;
      }
    })
    .catch(err => {
      showError(err.message || "Login failed");
    })
    .finally(() => {
      loginBtn.disabled = false;
      loginBtn.innerText = "Login";
    });

  function showError(msg) {
    errorEl.innerText = msg;
    errorEl.style.display = "block";
  }
}

function logout() {
  localStorage.clear();
  window.location.href = "login.html";
}

function toggleForgotPassword(event) {
  event.preventDefault();
  const forgotBox = document.getElementById("forgotPasswordBox");
  const forgotEmail = document.getElementById("forgotEmail");
  const loginHeader = document.getElementById("loginHeader");
  const loginFields = document.getElementById("loginFields");
  const loginError = document.getElementById("loginError");

  if (!forgotBox || !loginFields) return;

  loginFields.style.display = "none";
  forgotBox.style.display = "block";

  if (loginHeader) {
    loginHeader.style.display = "none";
  }

  if (loginError) {
    loginError.style.display = "none";
  }

  if (forgotEmail) {
    forgotEmail.value = document.getElementById("email")?.value?.trim() || "";
  }
}

function showLoginFields(event) {
  event.preventDefault();
  const forgotBox = document.getElementById("forgotPasswordBox");
  const loginFields = document.getElementById("loginFields");
  const loginHeader = document.getElementById("loginHeader");

  if (forgotBox) forgotBox.style.display = "none";
  if (loginFields) loginFields.style.display = "block";
  if (loginHeader) loginHeader.style.display = "block";
}

function forgotPassword() {
  const forgotEmailInput = document.getElementById("forgotEmail");
  const forgotMsg = document.getElementById("forgotMsg");
  const forgotBtn = document.getElementById("forgotBtn");

  if (!forgotEmailInput || !forgotMsg || !forgotBtn) return;

  const email = forgotEmailInput.value.trim();

  forgotMsg.style.display = "none";
  forgotMsg.style.color = "red";

  if (!email) {
    forgotMsg.innerText = "Email is required";
    forgotMsg.style.display = "block";
    return;
  }

  if (!email.includes("@")) {
    forgotMsg.innerText = "Invalid email format";
    forgotMsg.style.display = "block";
    return;
  }

  forgotBtn.disabled = true;
  forgotBtn.innerText = "Sending...";

  fetch(`${API_BASE}/auth/forgot-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email })
  })
    .then(async (res) => {
      const text = await res.text();
      if (!res.ok) {
        throw new Error(text || "Failed to reset password");
      }
      return text;
    })
    .then((message) => {
      forgotMsg.innerText = message || "Temporary password sent to your email";
      forgotMsg.style.color = "green";
      forgotMsg.style.display = "block";

      setTimeout(() => {
        window.location.href = `reset-password.html?email=${encodeURIComponent(email)}`;
      }, 800);
    })
    .catch((err) => {
      forgotMsg.innerText = err.message || "Failed to reset password";
      forgotMsg.style.color = "red";
      forgotMsg.style.display = "block";
    })
    .finally(() => {
      forgotBtn.disabled = false;
      forgotBtn.innerText = "Send Temporary Password";
    });
}


function pollOrderStatus(orderId) {
  const token = localStorage.getItem("jwt");
  const MAX_ATTEMPTS = 40; // ~2 minutes (40 * 3s)
  let attempts = 0;
  let lastStatus = null;
  const interval = setInterval(() => {
    attempts++;

    fetch(`${API_BASE}/orders/${orderId}`, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
      .then(res => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }
        return res.json();
      })
      .then(order => {
        document.getElementById("status").innerText =
          "Order Status: " + order.status;
        // Only react if status changed
        if (order.status !== lastStatus) {
          lastStatus = order.status;

          document.getElementById("status").innerText =
            "Order Status: " + order.status;

          console.log("Status changed to:", order.status);
        }
        //  STOP on terminal states
        /* if (
          order.status === "PAID" ||
          order.status === "DELIVERED" ||
          order.status === "FAILED" ||
          order.status === "CANCELLED"
        )  */
        if (
          order.status === "PAID" ||
          order.status === "DELIVERED" ||
          order.status === "FAILED" ||
          order.status === "CANCELLED"
        ) {
          clearInterval(interval);
          console.log("Polling stopped: final state");
        }

        //  STOP after max time
        if (attempts >= MAX_ATTEMPTS) {
          clearInterval(interval);
          console.warn("Polling stopped: timeout");
        }
      })
      .catch(err => {
        console.error("Polling error:", err.message);
        clearInterval(interval); // stop continuously fetching details from backend
      });

  }, 3000);
}


function loadRestaurants() {
  if (!localStorage.getItem("jwt")) {
    alert("Please login first");
    window.location.href = "login.html";
    return;
  }
  if (getUserRole() === "admin") {
    alert("Admins cannot browse restaurants");
    window.location.href = "admin.html";
    return;
  }

  fetch(`${API_BASE}/restaurants`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(restaurants => {
      if (!Array.isArray(restaurants)) {
        console.error("Expected restaurant array, got:", restaurants);
        return;
      }
      const container = document.getElementById("restaurants");
      renderRestaurantCards(restaurants, container);
    });
}
function openMenu(restaurantId) {
  if (!restaurantId) {
    console.error("Restaurant ID missing");
    return;
  }

  // store selected restaurant
  localStorage.setItem("selectedRestaurantId", restaurantId);

  // navigate to menu page
  window.location.href = "menu.html";
}

function requestRestaurantsByLocation(location, options = {}) {
  const { requireAuth = true } = options;
  const headers = {};
  const token = localStorage.getItem("jwt");

  if (requireAuth && token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return fetch(`${API_BASE}/restaurants/location?location=${encodeURIComponent(location)}`, {
    headers
  }).then((res) => {
    if (!res.ok) {
      throw new Error(`Failed to fetch restaurants by location (${res.status})`);
    }
    return res.json();
  });
}

function loadRestaurantsByLocation(location) {
  requestRestaurantsByLocation(location, { requireAuth: true })
    .then(restaurants => {
      if (!Array.isArray(restaurants)) {
        console.error("Expected restaurant array, got:", restaurants);
        return;
      }

      const container = document.getElementById("restaurants");
      renderRestaurantCards(restaurants, container);
    })
    .catch(err => {
      console.error("Failed to load restaurants by location:", err);
    });
}

function addToCart(item) {
  if (cart.length > 0 && cartRestaurantId && cartRestaurantId !== selectedRestaurantId) {
    const sourceRestaurant = cartRestaurantName || "another restaurant";
    const shouldReplace = window.confirm(
      `You already have items in cart from \"${sourceRestaurant}\".\nDo you want to clear the cart and add items from this restaurant?`
    );

    if (!shouldReplace) {
      renderCart();
      return;
    }

    cart = [];
  }

  if (!cartRestaurantId || cart.length === 0) {
    cartRestaurantId = selectedRestaurantId;
    cartRestaurantName = selectedRestaurantName;
    cartRestaurantImageUrl = selectedRestaurantImageUrl;
  }

  const existing = cart.find(i => i.itemId === item.itemId);

  if (existing) {
    existing.quantity++;
  } else {
    cart.push({
      itemId: item.itemId,
      name: item.name,
      price: item.price,
      quantity: 1
    });
  }

  renderCart();
}

function placeOrder() {
  const paymentMethod = getSelectedPaymentMethod();
  if (!localStorage.getItem("jwt")) {
    alert("Please login to place your order");
    return;
  }

  if (getUserRole() === "admin") {
    alert("Admins are not allowed to place orders");
    return;
  }

  console.log("Cart at checkout (just added for debugging):", cart);
  if (!cart || cart.length === 0) {
    alert("Your cart is empty");
    return;
  }
  //only proceeds after we select an address
  const addressId = document.getElementById("addressSelect")?.value;
  if (!addressId) {
    alert("Please select a delivery address");
    return;
  }

  //calculating this total in backend now
  /* // derive total from cart (NOT user input)
   const totalAmount = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  ); */


  if (!paymentMethod) {
    alert("Please select a payment method");
    return;
  }

  fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    /* body: JSON.stringify({
      restaurantId: selectedRestaurantId, 
      items: cart.map(item => ({
        itemId: item.itemId,
        name: item.name,
        price: item.price,
        quantity: item.quantity
      }))
    }) */
    body: JSON.stringify({
      restaurantId: cartRestaurantId || selectedRestaurantId,
      restaurantName: cartRestaurantName || selectedRestaurantName,
      restaurantImageUrl: cartRestaurantImageUrl || selectedRestaurantImageUrl,
      addressId: document.getElementById("addressSelect").value,
      paymentMethod: paymentMethod,
      items: cart.map(item => ({
        itemId: item.itemId,
        name: item.name,
        price: item.price,
        quantity: item.quantity
      }))
    })

  })
    .then(res => {
      if (!res.ok) {
        throw new Error("Order failed");
      }
      return res.json();
    })
    .then(order => {
      // clear cart after successful order
      clearCartState();
      renderCart();
      // STORE ORDER for Razorpay callback
      window.currentOrderId = order.id;
      if (paymentMethod === "CASH") {
        // CASH → normal flow
        // redirect to order tracking page
        window.location.href = `orders.html?orderId=${order.id}`;
        return;
      }

      // ONLINE PAYMENT → Razorpay
      setTimeout(() => {
        startRazorpayPayment(order.id, order.totalAmount);
      }, 500);
    })
    .catch(err => {
      console.error(err);
      alert("Failed to place order");
    });

}
//created->pickeup->outfordelivery->delivered
function renderDeliveryStatus(container, finalStatus) {
  if (!container || !finalStatus) return;

  const steps = ["CONFIRMED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"];
  const targetIndex = steps.indexOf(finalStatus);

  const stepEls = Array.from(container.querySelectorAll(".step"));

  // reset
  stepEls.forEach(el => {
    el.classList.remove("active");
    el.style.opacity = "0.3";
  });

  let current = 0;

  const interval = setInterval(() => {
    if (current > targetIndex) {
      clearInterval(interval);
      return;
    }

    stepEls[current].classList.add("active");
    stepEls[current].style.opacity = "1";

    current++;
  }, 400);
}

let ordersCache = [];
const ORDERS_PER_PAGE = 5;
let currentOrdersPage = 1;

function loadOrders() {
  if (!localStorage.getItem("jwt")) {
    alert("Please login first");
    window.location.href = "login.html";
    return;
  }
  if (getUserRole() === "admin") {
    alert("Admins cannot view orders");
    window.location.href = "admin.html";
    return;
  }
  const token = localStorage.getItem("jwt");
  fetch(`${API_BASE}/orders/user/me`, {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => res.json())
    .then(orders => {
      const list = document.getElementById("orders");
      const pagination = document.getElementById("ordersPagination");

      if (!Array.isArray(orders) || orders.length === 0) {
        list.innerHTML = "<li>No orders found.</li>";
        if (pagination) pagination.innerHTML = "";
        return;
      }
      ordersCache = [...orders].reverse();
      currentOrdersPage = 1;
      renderOrdersPage(currentOrdersPage);
    });
}

function renderOrdersPage(page) {
  const list = document.getElementById("orders");
  const pagination = document.getElementById("ordersPagination");

  if (!list) return;

  list.innerHTML = "";

  const totalPages = Math.ceil(ordersCache.length / ORDERS_PER_PAGE);
  currentOrdersPage = Math.min(Math.max(page, 1), totalPages);

  const start = (currentOrdersPage - 1) * ORDERS_PER_PAGE;
  const pageOrders = ordersCache.slice(start, start + ORDERS_PER_PAGE);

  pageOrders.forEach((order, index) => {
    renderOrderItem(order, start + index, list);
  });

  if (pagination) {
    pagination.innerHTML = "";

    const prevBtn = document.createElement("button");
    prevBtn.textContent = "Previous";
    prevBtn.disabled = currentOrdersPage === 1;
    prevBtn.onclick = () => renderOrdersPage(currentOrdersPage - 1);

    const pageInfo = document.createElement("span");
    pageInfo.textContent = `Page ${currentOrdersPage} of ${totalPages}`;

    const nextBtn = document.createElement("button");
    nextBtn.textContent = "Next";
    nextBtn.disabled = currentOrdersPage === totalPages;
    nextBtn.onclick = () => renderOrdersPage(currentOrdersPage + 1);

    pagination.appendChild(prevBtn);
    pagination.appendChild(pageInfo);
    pagination.appendChild(nextBtn);
  }
}
function formatOrderTime(dateTimeString) {
  if (!dateTimeString) return "N/A";
  const date = new Date(dateTimeString);
  if (Number.isNaN(date.getTime())) return "N/A";
  //return date.toLocaleTimeString("en-GB", { hour12: false });
  const datePart = date.toLocaleDateString("en-GB");
  const timePart = date.toLocaleTimeString("en-GB", { hour12: false });
  return `${datePart} ${timePart}`;
}

function renderOrderItem(order, index, list) {
  const li = document.createElement("li");
  li.style.cursor = "pointer;"
  li.innerHTML = `
    <div class="order-card">
      <div class="order-summary">
        <strong>Order #${index + 1}</strong>
        ${index === 0 ? `<span class="latest-tag">Latest Order</span>` : ""}<br/>
        Status: ${order.status}<br/>
        
      </div>

      <div class="delivery-status">
        <span class="step" data-step="CREATED">Confirmed</span>
        <span class="step" data-step="PICKED_UP">Picked Up</span>
        <span class="step" data-step="OUT_FOR_DELIVERY">Out for Delivery</span>
        <span class="step" data-step="DELIVERED">Delivered</span>
      </div>

      <div class="order-details" style="display:none">
        <p>Loading order details...</p>
      </div>
    </div>
  `;

  li.onclick = () => toggleOrderDetails(order.id, li);

  const statusContainer = li.querySelector(".delivery-status");
  renderDeliveryStatus(statusContainer, order.status);

  list.appendChild(li);
}

function toggleOrderDetails(orderId, orderElement) {

  const detailsDiv = orderElement.querySelector(".order-details");

  // toggle if already loaded
  if (detailsDiv.style.display === "block") {
    detailsDiv.style.display = "none";
    return;
  }

  // collapse others (optional but nice UX)
  document.querySelectorAll(".order-details").forEach(d => {
    d.style.display = "none";
  });

  fetch(`${API_BASE}/orders/${orderId}`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(order => {

      detailsDiv.innerHTML = `
      <hr>
      <strong>Order Details</strong>
      <ul>
        ${order.items.map(i =>
        `<li>${i.name} x ${i.quantity} = ₹${i.price}</li>`
      ).join("")}
      </ul>
      <strong>Total: ₹${order.totalAmount}</strong><br/>
      <strong>Ordered At:</strong> ${formatOrderTime(order.orderedAt)}<br/>
      <strong>Delivered At:</strong> ${formatOrderTime(order.deliveredAt)}<br/>
      <strong>Ordered Place:</strong> ${order.restaurantName || order.restaurantId || "N/A"}
    `;

      detailsDiv.style.display = "block";
    });
}

/* function showOrderDetails(orderId) {
  fetch(`${API_BASE}/orders/${orderId}`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(order => {
      const div = document.getElementById("orderDetails");

      let itemsHtml = "";

      order.items.forEach(item => {
        itemsHtml += `
          <li>
            ${item.name} x ${item.quantity}
            = ₹${item.price * item.quantity}
          </li>
        `;
      });

      div.innerHTML = `
        <h3>Order details</h3>
        <p><b>Status:</b> ${order.status}</p>

        <ul>
          ${itemsHtml}
        </ul>

        <hr>
        <p><b>Total Amount:</b> ₹${order.totalAmount}</p>
      `;
    })
    .catch(err => {
      console.error(err);
      alert("Failed to load order details");
    });
} */

function trackOrder(orderId) {
  pollOrderStatus(orderId);
}

function loadMenu() {
  if (getUserRole() === "admin") {
    alert("Admins cannot view menus");
    window.location.href = "admin.html";
    return;
  }
  selectedRestaurantId = localStorage.getItem("selectedRestaurantId");

  if (!selectedRestaurantId) {
    alert("No restaurant selected");
    window.location.href = "restaurants.html";
    return;
  }
  loadCartStateFromStorage();
  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}`, {
    headers: localStorage.getItem("jwt")
      ? { "Authorization": `Bearer ${localStorage.getItem("jwt")}` }
      : {}
  })
    .then(res => res.json())
    .then(r => {
      if (r.open === false) {
        alert("This restaurant is currently closed.");
        window.location.href = "restaurants.html";
        return;
      }
      document.getElementById("restaurantName").innerText = r.name;
      selectedRestaurantName = r.name || null;
      selectedRestaurantImageUrl = r.imageUrl || null;
      //loadAddresses(r.location);
      currentRestaurantLocation = r.location;
      setupCheckoutAccess();
      if (localStorage.getItem("jwt")) {
        loadAddresses(r.location);
      }

      // auto-fill address form
      const locInput = document.getElementById("addrLocation");
      if (locInput) {
        locInput.value = r.location;
        locInput.readOnly = true;
      }

      const menuEl = document.getElementById("menu");
      //menuEl.innerHTML = "";
      if (!Array.isArray(r.menu)) {
        console.error("Menu is not an array:", r.menu);
        return;
      }
      renderMenuItems(r.menu);
      renderCart();
    });
}
        function renderMenuItems(menuItems = []) {
  const menuEl = document.getElementById("menu");
  if (!menuEl) return;
  menuEl.innerHTML = "";

       const filteredItems = menuItems.filter(item => {
    if (menuDietFilter === "veg") return item.isVeg === true;
    if (menuDietFilter === "non-veg") return item.isVeg !== true;
    return true;
  });
if (filteredItems.length === 0) {
    menuEl.innerHTML = "<li>No menu items match this filter.</li>";
    return;
  }

  filteredItems.forEach(item => {
    const li = document.createElement("li");
    const isAvailable = item.available !== false;
    const vegLabel = item.isVeg === true ? "Veg" : "Non-veg";
    li.style.alignItems = "center";
    li.style.gap = "12px";

    li.innerHTML = `
      <img
        src="${item.imageUrl || '/images/default-food.png'}"
        style="
          width:80px;
          height:60px;
          object-fit:cover;
          border-radius:6px;
        "
      />

      <div style="flex:1">
        <strong>${item.name}</strong><br>
        ₹${item.price}<br>
        <small style="color:#666;">${vegLabel}</small>
        ${isAvailable ? "" : '<br><small style="color:#c62828;font-weight:600;">Out of stock</small>'}
      </div>
    `;

    if (getUserRole() !== "admin") {
      const btn = document.createElement("button");
      btn.innerText = isAvailable ? "Add" : "Out of stock";
      btn.disabled = !isAvailable;
      btn.onclick = () => addToCart(item);
      li.appendChild(btn);
    }
    menuEl.appendChild(li);
  });
}


/* function addItem(item) {
  selectedItems.push(item);
  updateTotal();
} */

/* function updateTotal() {
  const total = selectedItems.reduce((sum, i) => sum + i.price, 0);
  document.getElementById("total").innerText = total;
} */

function renderCart() {
  const cartEl = document.getElementById("cart");
  if (!cartEl) return;

  cartEl.innerHTML = "";

  const cartRestaurantNotice = document.getElementById("cartRestaurantNotice");
  if (cartRestaurantNotice) {
    if (cart.length > 0 && cartRestaurantName) {
      cartRestaurantNotice.innerText = `Items from: ${cartRestaurantName}`;
      cartRestaurantNotice.style.display = "block";
    } else {
      cartRestaurantNotice.innerText = "";
      cartRestaurantNotice.style.display = "none";
    }
  }
  let total = 0;

  cart.forEach((item, index) => {
    total += item.price * item.quantity;

    const li = document.createElement("li");
    li.style.marginBottom = "10px";

    li.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <strong>${item.name}</strong><br/>
          ₹${item.price} x ${item.quantity} = ₹${item.price * item.quantity}
        </div>

        <div style="display:flex; gap:6px; align-items:center;">
          <button onclick="decreaseQty(${index})">−</button>
          <span>${item.quantity}</span>
          <button onclick="increaseQty(${index})">+</button>
          <button onclick="removeFromCart(${index})">Remove</button>
        </div>
      </div>
    `;
    cartEl.appendChild(li);
  });

  document.getElementById("total").innerText = total;
  setupCheckoutAccess();
  saveCartState();
}

function setupCheckoutAccess() {
  const isLoggedIn = Boolean(localStorage.getItem("jwt"));
  const loginCta = document.getElementById("loginToPlaceOrderBtn");
  const addressSection = document.getElementById("addressSection");
  const paymentSection = document.getElementById("paymentSection");
  const placeOrderBtn = document.getElementById("placeOrderBtn");

  if (!loginCta || !addressSection || !paymentSection || !placeOrderBtn) return;

  if (isLoggedIn) {
    loginCta.style.display = "none";
    addressSection.style.display = "block";
    paymentSection.style.display = "block";
    enablePlaceOrder();
    return;
  }

  addressSection.style.display = "none";
  paymentSection.style.display = "none";
  placeOrderBtn.disabled = true;
  loginCta.style.display = cart.length > 0 ? "block" : "none";
}

function openLoginPopup() {
  const popup = window.open(
    `login.html?popup=1&next=${encodeURIComponent("menu.html")}`,
    "loginPopup",
    "width=480,height=720"
  );

  if (!popup) {
    alert("Popup blocked. Please allow popups and try again.");
  }
}

function registerAuthListeners() {
  
    const refreshPostLoginUi = () => {
    setupCheckoutAccess();
    
    if (typeof renderNavbar === "function") {
      renderNavbar();
    }
    if (currentRestaurantLocation) {
      loadAddresses(currentRestaurantLocation);
    }
    };

  window.addEventListener("message", (event) => {
    if (event.origin !== window.location.origin) return;
    if (event.data?.type !== "fd_login_success") return;

    refreshPostLoginUi();
  });

  window.addEventListener("storage", (event) => {
    if (event.key !== "jwt" || !event.newValue) return;
    refreshPostLoginUi();
  });
}

function searchLandingByLocation() {
  const input = document.getElementById("landingLocationInput");
  const location = input?.value.trim();

  if (!location) {
    showServiceabilityPopup("Please enter a location to continue.");
    return;
  }

  requestRestaurantsByLocation(location, { requireAuth: false })
    .then(restaurants => {
      const resultsSection = document.getElementById("landingResults");
      const locationTag = document.getElementById("landingLocationTag");
      const container = document.getElementById("landingRestaurants");

      if (!Array.isArray(restaurants) || restaurants.length === 0) {
        if (resultsSection) resultsSection.style.display = "none";
        showServiceabilityPopup("We are not currently serving in your location.");
        return;
      }

      if (resultsSection) resultsSection.style.display = "block";
      if (locationTag) locationTag.textContent = location;
      renderRestaurantCards(restaurants, container);
    })
    .catch((err) => {
      console.error("Landing location search failed:", err);
      showServiceabilityPopup("We are not currently serving in your location.");
    });
}

function renderRestaurantCards(restaurants, container) {
  if (!container) return;
  container.innerHTML = "";
  const openRestaurants = restaurants.filter(r => r.open !== false);
  const filteredRestaurants = openRestaurants.filter(r => {
    const menu = Array.isArray(r.menu) ? r.menu : [];
    if (restaurantDietFilter === "veg") {
      return menu.some(item => item.isVeg === true);
    }
    if (restaurantDietFilter === "non-veg") {
      return menu.some(item => item.isVeg !== true);
    }
    return true;
  });
  if (filteredRestaurants.length === 0) {
    container.innerHTML = "<p>No restaurants found</p>";
    return;
  }

  filteredRestaurants.forEach(r => {
    const card = document.createElement("div");
    card.className = "restaurant-card";
    card.innerHTML = `
      <img src="${r.imageUrl || '/images/default-restaurant.png'}" style="width:100%;height:140px;object-fit:cover;border-radius:6px;"/>
      <h3>${r.name}</h3>
      <p>${r.location}</p>
      <button onclick="openMenu('${r.id}')">View Menu</button>
    `;
    container.appendChild(card);
  });
}

function showServiceabilityPopup(message) {
  const modal = document.getElementById("serviceabilityModal");
  const modalMessage = document.getElementById("serviceabilityMessage");
  if (!modal || !modalMessage) {
    alert(message);
    return;
  }

  modalMessage.textContent = message;
  modal.style.display = "flex";
}

function closeServiceabilityPopup() {
  const modal = document.getElementById("serviceabilityModal");
  if (modal) {
    modal.style.display = "none";
  }
}
function setRestaurantDietFilter() {
  const dropdown = document.getElementById("dishTypeFilter");
  if (!dropdown) return;
  restaurantDietFilter = dropdown.value;
  const locationDropdown = document.getElementById("locationFilter");
  if (locationDropdown && locationDropdown.value) {
    loadRestaurantsByLocation(locationDropdown.value);
    return;
  }
  loadRestaurants();
}
function setMenuDietFilter() {
  const dropdown = document.getElementById("menuDishTypeFilter");
  if (!dropdown) return;
  menuDietFilter = dropdown.value;
  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}`, {
    headers: localStorage.getItem("jwt")
      ? { "Authorization": `Bearer ${localStorage.getItem("jwt")}` }
      : {}
  })
    .then(res => res.json())
    .then(r => renderMenuItems(Array.isArray(r.menu) ? r.menu : []))
    .catch(err => console.error("Failed to apply menu filter", err));
}
function increaseQty(index) {
  cart[index].quantity++;
  renderCart();
}

function decreaseQty(index) {
  cart[index].quantity--;

  if (cart[index].quantity <= 0) {
    cart.splice(index, 1); // remove item
  }
  renderCart();
}

function removeFromCart(index) {
  cart.splice(index, 1);
  renderCart();
}

//fetch list of locations from backend to populate in dropdown
function loadLocations() {
  fetch(`${API_BASE}/restaurants/locations`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(locations => {
      if (!Array.isArray(locations)) {
        console.error("Expected array but got:", locations);
        return;
      }
      const dropdown = document.getElementById("locationFilter");

      locations.forEach(loc => {
        const opt = document.createElement("option");
        opt.value = loc;
        opt.textContent = loc;
        dropdown.appendChild(opt);
      });
    });
}
//location dropdown functionality
function filterByLocation() {
  const location = document.getElementById("locationFilter").value;

  if (!location) {
    loadRestaurants(); // fallback to existing API
  } else {
    loadRestaurantsByLocation(location);
  }
}

function loadAddresses(location) {

  if (!location) return;
  fetch(`${API_BASE}/auth/addresses?location=${encodeURIComponent(location)}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(addresses => {
      const select = document.getElementById("addressSelect");
      select.innerHTML = `<option value="">Select address</option>`;

      addresses.forEach(a => {
        const opt = document.createElement("option");
        opt.value = a.addressId;
        opt.textContent = `${a.label} - ${a.line1}`;
        if (a.isDefault) {
          opt.selected = true;
        }
        select.appendChild(opt);
      });
      enablePlaceOrder();
    });
}

function enablePlaceOrder() {
  const selected = document.getElementById("addressSelect").value;
  document.getElementById("placeOrderBtn").disabled = !selected;
}

function saveAddress() {

  if (getUserRole() === "admin") {
    alert("Admins cannot add addresses");
    return;
  }

  const address = {
    label: document.getElementById("addrLabel").value,
    line1: document.getElementById("addrLine1").value,
    location: document.getElementById("addrLocation").value,
    pincode: document.getElementById("addrPincode").value
  };

  // basic validation
  if (!address.label || !address.line1 || !address.location) {
    alert("Label, address and location are required");
    return;
  }

  const method = editingAddressId ? "PUT" : "POST";
  const url = editingAddressId
    ? `${API_BASE}/auth/addresses/${editingAddressId}`
    : `${API_BASE}/auth/addresses`;

  fetch(url, {
    method,
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify(address)
  })
    .then(() => {
      alert(editingAddressId ? "Address updated" : "Address added");

      resetForm();

      // refresh address list page
      if (document.getElementById("addressList")) {
        loadAddressesList();
      }

      // refresh dropdown if present (menu page)
      const location = document.getElementById("addrLocation")?.value;
      if (location && document.getElementById("addressSelect")) {
        loadAddresses(location);
      }
    })
    .catch(err => {
      console.error(err);
      alert("couldn't add address");
    });
}

function resetForm() {
  editingAddressId = null;

  const label = document.getElementById("addrLabel");
  const line1 = document.getElementById("addrLine1");
  const location = document.getElementById("addrLocation");
  const pincode = document.getElementById("addrPincode");
  const saveBtn = document.getElementById("saveBtn");

  if (label) label.value = "";
  if (line1) line1.value = "";
  if (location) location.value = "";
  if (pincode) pincode.value = "";

  if (saveBtn) saveBtn.innerText = "Save Address";
}

function toggleMenu(id, event) {
  if (event) event.stopPropagation(); // prevent document click
  const menu = document.getElementById(`menu-${id}`);
  if (!menu) return;

  const isOpen = menu.style.display === "block";

  // close all first
  closeAllMenus();

  // toggle current
  if (!isOpen) {
    menu.style.display = "block";
  }
}

// close menu when clicking outside
document.addEventListener("click", function (e) {
  if (!e.target.closest(".menu-container")) {
    closeAllMenus();
  }
});


function closeAllMenus() {
  document.querySelectorAll(".menu-dropdown")
    .forEach(m => m.style.display = "none");
}

function markAsDefault(addressId) {
  fetch(`${API_BASE}/auth/addresses/${addressId}/default`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Failed to set default");

    })
    .then(() => {
      closeAllMenus();
      loadAddressesList();
    })
    .catch(err => {
      console.error(err);
      alert("Failed to set default address");
    });
}
// Load all user addresses (no location filter here)
function loadAddressesList() {
  fetch(`${API_BASE}/auth/addresses/all`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(addresses => {
      const ul = document.getElementById("addressList");
      ul.innerHTML = "";

      addresses.forEach(a => {
        const li = document.createElement("li");

        li.innerHTML = `
        <div class="address-card">
          <div class="address-info">
            <b>${a.label}</b><br>
            ${a.isDefault ? '<span class="default-tag">(Default)</span>' : ''}
            <br>
            ${a.line1}<br>
            ${a.location} - ${a.pincode}
          </div>

          <div class="menu-container">
            <button class="menu-btn" onclick="toggleMenu('${a.addressId}', event)">⋮</button>

            <div class="menu-dropdown" id="menu-${a.addressId}">
              <button onclick="startEditAddress(
                '${a.addressId}',
                '${a.label}',
                '${a.line1}',
                '${a.location}',
                '${a.pincode}'
              ); closeAllMenus();">
                ✏️ Edit
              </button>

              ${!a.isDefault
            ? `<button onclick="markAsDefault('${a.addressId}'); closeAllMenus();">
                       ⭐ Set as Default
                     </button>`
            : ""
          }

              <button
                onclick="deleteAddress('${a.addressId}'); closeAllMenus();"
                ${a.isDefault ? 'disabled title="Default address cannot be deleted"' : ''}
              >
                🗑️ Delete
              </button>
            </div>
          </div>
        </div>
      `;

        ul.appendChild(li);
      });
    });
}

function startEditAddress(id, label, line1, location, pincode) {
  editingAddressId = id;

  document.getElementById("addrLabel").value = label;
  document.getElementById("addrLine1").value = line1;
  document.getElementById("addrLocation").value = location;
  document.getElementById("addrPincode").value = pincode;

  document.getElementById("saveBtn").innerText = "Update Address";
}
function deleteAddress(id) {
  if (!confirm("Delete this address?")) return;

  fetch(`${API_BASE}/auth/addresses/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(() => loadAddressesList());
}
function editAddress(id) {
  const label = prompt("Label:");
  const line1 = prompt("Address line:");
  const location = prompt("Location:");
  const pincode = prompt("Pincode:");

  if (!label || !line1 || !location) {
    alert("All fields required");
    return;
  }

  fetch(`${API_BASE}/auth/addresses/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify({ label, line1, location, pincode })
  })
    .then(() => loadAddressesList());
}

function getSelectedPaymentMethod() {
  const container = document.getElementById("paymentMethods");
  if (!container) {
    return null;
  }
  const selected = container.querySelector("input[type='radio']:checked");
  return selected ? selected.value : null;
}

async function startRazorpayPayment(orderId, amount, retries = 5) {
  try {
    const res = await fetch(`${API_BASE}/payments/razorpay/order`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${localStorage.getItem("jwt")}`
      },
      body: JSON.stringify({
        orderId: orderId,
        amount: amount
      })
    });

    if (!res.ok) {
      throw new Error("Payment not ready yet");
    }

    const data = await res.json();
    openRazorpayCheckout(data, orderId);

  } catch (err) {
    if (retries > 0) {
      console.log("Waiting for payment creation… retrying");
      setTimeout(() => {
        startRazorpayPayment(orderId, amount, retries - 1);
      }, 300);
    } else {
      alert("Payment service not ready. Please try again.");
    }
  }
}

function openRazorpayCheckout(data, orderId) {

  const options = {
    key: "rzp_test_S096iHo0OtCLup", // ONLY key ID (safe)
    amount: data.amount,
    currency: data.currency,
    name: "Food Delivery App",
    description: "Order Payment",
    order_id: data.razorpayOrderId,

    handler: function (response) {
      verifyPaymentOnBackend(orderId,
        response.razorpay_payment_id,
        response.razorpay_order_id,
        response.razorpay_signature
      );
    }, modal: {
      ondismiss: function () {
        notifyPaymentFailure(orderId, data.razorpayOrderId);
      }
    }

    , theme: {
      color: "#3399cc"
    }
  };

  const rzp = new Razorpay(options);
  rzp.open();
}
function notifyPaymentFailure(orderId, razorpayOrderId) {
  fetch(`${API_BASE}/payments/razorpay/failure`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify({
      orderId: orderId,
      razorpayOrderId: razorpayOrderId
    })
  });

  alert("Payment failed. Order was not placed.");
  window.location.href = "menu.html";
}

function verifyPaymentOnBackend(orderId, paymentId, razorpayOrderId, signature) {
  fetch(`${API_BASE}/payments/razorpay/verify`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify({
      orderId: orderId,
      razorpayPaymentId: paymentId,
      razorpayOrderId: razorpayOrderId,
      razorpaySignature: signature
    })
  })
    .then(res => {
      //payment failed
      if (!res.ok) throw new Error("Payment verification failed");
      return;
    })
    .then(() => {
      // Redirect AFTER verification(payment success)
      window.location.href = `orders.html?orderId=${orderId}`;
    })
    .catch(err => {
      console.error(err);
      alert("Payment verification failed");
      window.location.href = "menu.html";

    });
}



function searchRestaurants() {
  const query = document.getElementById("searchInput").value.trim();

  if (query.length === 0) {
    document.getElementById("searchSuggestions").innerHTML = "";
    loadRestaurants();
    return;
  }

  // fallback → load all
  if (query.length === 0) {
    loadRestaurants();
    return;
  }

  // debounce
  clearTimeout(searchTimeout);

  searchTimeout = setTimeout(() => {
    fetch(`${API_BASE}/restaurants/search?q=${encodeURIComponent(query)}`, {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${localStorage.getItem("jwt")}`
      }
    })
      .then(res => res.json())
      .then(results => {
        renderSearchResults(results);
        renderSuggestions(results);
      })

      .catch(err => console.error("Search failed", err));
  }, 300);
}

function renderSearchResults(results) {
  const container = document.getElementById("restaurants");
  container.innerHTML = "";

  if (!Array.isArray(results) || results.length === 0) {
    container.innerHTML = "<p>No results found</p>";
    return;
  }

  const openResults = results.filter(r => r.open !== false);
  if (openResults.length === 0) {
    container.innerHTML = "<p>No results found</p>";
    return;
  }
  openResults.forEach(r => {
    const card = document.createElement("div");
    card.className = "restaurant-card";

    card.innerHTML = `
      <img 
        src="${r.imageUrl || '/images/default-restaurant.png'}"
        style="width:100%;height:140px;object-fit:cover;border-radius:6px;"
      />
      <h3>${r.restaurantName}</h3>
      <p>${r.location}</p>
      ${r.matchedMenus.length > 0
        ? `<p style="color:green;font-size:13px;">
               Matches: ${r.matchedMenus.join(", ")}
             </p>`
        : ""
      }
      <button onclick="openMenu(${r.restaurantId})">View Menu</button>
    `;
    container.appendChild(card);
  });
}
//autofill functionality
function renderSuggestions(results) {
  const box = document.getElementById("searchSuggestions");
  box.innerHTML = "";
  if (!Array.isArray(results) || results.length === 0) return;

  results.slice(0, 5).forEach(r => {
    const div = document.createElement("div");
    div.className = "suggestion-item";
    div.innerHTML = `
  <strong>${r.restaurantName}</strong>
  ${Array.isArray(r.matchedMenus) && r.matchedMenus.length > 0
        ? `<div style="font-size:12px; color:gray;">
           ${r.matchedMenus.join(", ")}
         </div>`
        : ""
      }
`;


    div.onclick = () => {
      document.getElementById("searchInput").value = r.restaurantName;
      box.innerHTML = "";
      window.location.href = `menu.html?restaurantId=${r.restaurantId}`;
    };

    box.appendChild(div);
  });
}
