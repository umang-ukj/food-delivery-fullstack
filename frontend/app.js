let token = "";
const API_BASE = "http://localhost:8080";
let selectedRestaurantId = null;
//let selectedItems = [];
let cart = [];
let editingAddressId = null;
let index=0;
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

  if (!email || !password) {
    alert("Email and password are required");
    return;
  }

  if (!email.includes("@")) {
    alert("Invalid email format");
    return;
  }
  

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

  if (role === "admin") {
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
    const ul = document.getElementById("restaurants");
    ul.innerHTML = "";

    restaurants.forEach(r => {
      const li = document.createElement("li");
      li.innerText =  `${r.name}`;
      li.onclick = () => {
        window.location.href = `menu.html?restaurantId=${r.id}`;
      };
      ul.appendChild(li);
    });
  });
}

function loadRestaurantsByLocation(location) {
  fetch(`${API_BASE}/restaurants/location?location=${encodeURIComponent(location)}`, {
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
      li.innerText =  `${r.name}`;
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
const paymentMethod = getSelectedPaymentMethod();
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
  restaurantId: selectedRestaurantId,
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
    cart = [];
    // STORE ORDER for Razorpay callback
  window.currentOrderId = order.id;
  if (paymentMethod === "CASH") {
    // CASH → normal flow
    // redirect to order tracking page
    window.location.href = `orders.html?orderId=${order.id}`;
    return;
  }

  // ONLINE PAYMENT → Razorpay
  startRazorpayPayment(order.id, order.totalAmount);
    
  })
  .catch(err => {
    console.error(err);
    alert("Failed to place order");
  });
  
}
//created->pickeup->outfordelivery->delivered
function renderDeliveryStatus(container, finalStatus) {
  if (!container || !finalStatus) return;

  const steps = ["CREATED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"];
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
  }, 700); //  visible transition
}


function loadOrders() {
  if (!localStorage.getItem("jwt")) {
    alert("Please login first");
  window.location.href = "login.html";
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
    list.innerHTML = "";

    /* orders.forEach(order => {
      const li = document.createElement("li");
      li.innerText = `Order #${order.id} - ${order.status}`;
       li.style.cursor = "pointer";

        li.onclick = () => showOrderDetails(order.id);
        list.appendChild(li);
    }); */
    /* orders.forEach((order, index) => {
  const li = document.createElement("li");
    li.innerText = `Order #${index + 1} - ${order.status}`;
    li.style.cursor = "pointer";
    li.onclick = () => showOrderDetails(order.id);
    list.appendChild(li);
}); */
orders.forEach((order, index) => {
  const li = document.createElement("li");

  li.innerHTML = `
    <div class="order-card">
      <div class="order-summary">
        <strong>Order #${index + 1}</strong><br/>
        Status: ${order.status}
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

  renderDeliveryStatus(li, order.status);
list.appendChild(li);
});


  });
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
      <strong>Total: ₹${order.totalAmount}</strong>
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

      loadAddresses(r.location);

    // auto-fill address form
    const locInput = document.getElementById("addrLocation");
    if (locInput) {
      locInput.value = r.location;
      locInput.readOnly = true;
    }

      const menuEl = document.getElementById("menu");
      menuEl.innerHTML = "";

      r.menu.forEach(item => {
        const li = document.createElement("li");

        li.innerHTML = `${item.name} - ₹${item.price}`;

if (getUserRole() !== "admin") {
  const btn = document.createElement("button");
  btn.innerText = "Add";
  btn.onclick = () => addToCart(item);
  li.appendChild(btn);
}
        //li.querySelector("button").onclick = () => addToCart(item);
        menuEl.appendChild(li);
      });
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
  cartEl.innerHTML = "";

  let total = 0;

  cart.forEach(item => {
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
          <button onclick="removeFromCart(${index})">❌</button>
        </div>
      </div>
    `;
    cartEl.appendChild(li);
  });

  document.getElementById("total").innerText = total;
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

  const location = document.getElementById("addrLocation")?.value;
  if (location) {
    loadAddresses(location); // refresh dropdown
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


function toggleMenu(id) {
  document.querySelectorAll(".menu-dropdown")
    .forEach(m => m.style.display = "none");

  const menu = document.getElementById(`menu-${id}`);
  menu.style.display = menu.style.display === "block" ? "none" : "block";
}

// close menu when clicking outside
document.addEventListener("click", e => {
  if (!e.target.classList.contains("menu-btn")) {
    document.querySelectorAll(".menu-dropdown")
      .forEach(m => m.style.display = "none");
  }
});
function markAsDefault(addressId) {
  fetch(`${API_BASE}/auth/addresses/${addressId}/default`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    }
  })
  .then(res => {
    if (!res.ok) throw new Error();
    loadAddressesList();   // refresh UI
  })
  .catch(() => alert("Failed to mark default address"));
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
  <div>
    <b>${a.label}</b><br>
    ${a.isDefault ? '<span style="color:green;margin-left:8px;">(Default)</span>' : ''}
    <br>
    ${a.line1}<br>
    ${a.location} - ${a.pincode}
  </div>
  <div class="menu-container">
  <button class="menu-btn" onclick="toggleMenu('${a.addressId}')">⋮</button>

  <div class="menu-dropdown" id="menu-${a.addressId}">
    <button onclick="startEditAddress(
  '${a.addressId}',
  '${a.label}',
  '${a.line1}',
  '${a.location}',
  '${a.pincode}'
)">✏️ Edit</button>
${!a.isDefault ? 
  `<button onclick="markAsDefault('${a.addressId}')">⭐ Set as Default</button>` : ''}

   <button onclick="deleteAddress('${a.addressId}')"
   ${a.isDefault ? 'disabled title="Default address cannot be deleted"' : ''}>🗑️ Delete </button>
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

function startRazorpayPayment(orderId, amount) {

  fetch(`${API_BASE}/payments/razorpay/order`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${localStorage.getItem("jwt")}`
    },
    body: JSON.stringify({
      orderId: orderId,
      amount: amount
    })
  })
  .then(res => res.json())
  .then(data => openRazorpayCheckout(data, orderId));
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
    },

    theme: {
      color: "#3399cc"
    }
  };

  const rzp = new Razorpay(options);
  rzp.open();
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
    if (!res.ok) throw new Error("Payment verification failed");
    return ;
  })
  .then(() => {
    // Redirect AFTER verification
    window.location.href = `orders.html?orderId=${orderId}`;
  })
  .catch(err => {
    console.error(err);
    alert("Payment verification failed");
  });
}


