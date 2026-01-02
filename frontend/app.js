let token = "";
const API_BASE = "http://localhost:8080";
let selectedRestaurantId = null;
let selectedItems = [];
let cart = [];

function login() {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  })
  .then(res => {
    if (!res.ok) throw new Error("Login failed");
    return res.json();
  })
  .then(data => {
    // store auth info
    localStorage.setItem("jwt", data.token);
    localStorage.setItem("userEmail", email);
    const payload = JSON.parse(atob(data.token.split(".")[1]));
    const role = payload.role;

  if (role === "ADMIN") {
    window.location.href = "admin.html";
  } else {
    window.location.href = "restaurants.html";
  }
  })
  .catch(() => alert("Login failed"));
}

function logout() {
  localStorage.clear();
  window.location.href = "index.html";
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
}

  fetch(`${API_BASE}/restaurants`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
  .then(res => res.json())
  .then(restaurants => {
    const ul = document.getElementById("restaurants");
    ul.innerHTML = "";

    restaurants.forEach(r => {
      const li = document.createElement("li");
      li.innerText = r.name;
      li.onclick = () => {
        window.location.href = `menu.html?restaurantId=${r.id}`;
      };
      ul.appendChild(li);
    });
  });
}

function addToCart(item) {
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
  console.log("Cart at checkout (just added for debugging):", cart); 
  if (!cart || cart.length === 0) {
    alert("Your cart is empty");
    return;
  }

  //calculating this total in backend now
  /* // derive total from cart (NOT user input)
   const totalAmount = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  ); */

  fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify({
      restaurantId: selectedRestaurantId, 
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
    cart = [];

    // redirect to order tracking page
    window.location.href = `orders.html?orderId=${order.id}`;
  })
  .catch(err => {
    console.error(err);
    alert("Failed to place order");
  });
  
}

function loadOrders() {
  if (!localStorage.getItem("jwt")) {
    alert("Please login first");
  window.location.href = "login.html";
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
    list.innerHTML = "";

    orders.forEach(order => {
      const li = document.createElement("li");
      li.innerText = `Order #${order.id} - ${order.status}`;
       li.style.cursor = "pointer";

        li.onclick = () => showOrderDetails(order.id);
        list.appendChild(li);
    });
  });
}
function showOrderDetails(orderId) {
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
        <h3>Order #${order.id}</h3>
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
}

function trackOrder(orderId) {
  pollOrderStatus(orderId);
}

function loadMenu() {
  const params = new URLSearchParams(window.location.search);
  selectedRestaurantId = params.get("restaurantId");

  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}`, {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    }
  })
    .then(res => res.json())
    .then(r => {
      document.getElementById("restaurantName").innerText = r.name;

      const menuEl = document.getElementById("menu");
      menuEl.innerHTML = "";

      r.menu.forEach(item => {
        const li = document.createElement("li");

        li.innerHTML = `
          ${item.name} - ₹${item.price}
          <button type="button">Add</button>
        `;

        li.querySelector("button").onclick = () => addToCart(item);
        menuEl.appendChild(li);
      });
    });
}


function addItem(item) {
  selectedItems.push(item);
  updateTotal();
}

function updateTotal() {
  const total = selectedItems.reduce((sum, i) => sum + i.price, 0);
  document.getElementById("total").innerText = total;
}

function renderCart() {
  const cartEl = document.getElementById("cart");
  cartEl.innerHTML = "";

  let total = 0;

  cart.forEach(item => {
    total += item.price * item.quantity;

    const li = document.createElement("li");
    li.innerText =
      `${item.name} x ${item.quantity} = ₹${item.price * item.quantity}`;
    cartEl.appendChild(li);
  });

  document.getElementById("total").innerText = total;
}