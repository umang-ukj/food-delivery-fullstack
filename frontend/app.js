let token = "";
const API_BASE = "http://localhost:8080";

function login() {
  fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      email: document.getElementById("email").value,
      password: document.getElementById("password").value
    })
  })
  .then(res => res.json())
  .then(data => {
    token = data.token;
    localStorage.setItem("jwt", token);
    window.location.href = "order.html";
  })
  .catch(() => alert("Login failed"));
}

function placeOrder() {
  const token = localStorage.getItem("jwt");

  fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      userId: 1,
      restaurantId: document.getElementById("restaurantId").value,
      totalAmount: document.getElementById("amount").value
    })
  })
  .then(res => res.json())
  .then(order => {
    document.getElementById("status").innerText =
      "Order Created. ID: " + order.id;

    pollOrderStatus(order.id);
  });
}

function pollOrderStatus(orderId) {
  const token = localStorage.getItem("jwt");

  const interval = setInterval(() => {
    fetch(`${API_BASE}/orders/${orderId}`, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
    .then(res => res.json())
    .then(order => {
      document.getElementById("status").innerText =
        "Order Status: " + order.status;

      if (order.status === "DELIVERED" || order.status === "FAILED") {
        clearInterval(interval);
      }
    });
  }, 3000);
}
