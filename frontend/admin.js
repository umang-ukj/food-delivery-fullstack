const API_BASE = "http://localhost:8080";
let selectedRestaurantId = null;

//prevents user from opening admin panel manually
(function protectAdmin() {
  const token = localStorage.getItem("jwt");
  if (!token) {
    window.location.href = "login.html";
    return;
  }

  const payload = JSON.parse(atob(token.split(".")[1]));
  if (payload.role !== "admin") {
    alert("Access denied");
    window.location.href = "index.html";
  }
})();

function authHeaders() {
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${localStorage.getItem("jwt")}`
  };
}

function showAddRestaurant() {
  document.getElementById("content").innerHTML = `
    <div class="admin-form-card">
      <h3>Add Restaurant</h3>
      <div class="admin-form-grid">
        <label>Restaurant name
          <input id="rname" class="admin-input" placeholder="Restaurant Name">
        </label>
        <label>Location
          <input id="loc" class="admin-input" placeholder="Location">
        </label>
        <label>Restaurant image
          <input type="file" id="restaurantImage" class="admin-file-input" accept="image/*">
        </label>
      </div>
      <label class="admin-check">
        <input type="checkbox" id="open" checked> Open
      </label>
      <button class="primary-btn admin-action-btn" onclick="addRestaurant()">Save Restaurant</button>
    </div>
  `;
}

async function addRestaurant() {
  const name = document.getElementById("rname").value.trim();
  const location = document.getElementById("loc").value.trim();
  const imageInput = document.getElementById("restaurantImage");
  const imageFile = imageInput ? imageInput.files[0] : null;
  let imageUrl = null;
  //let open = true; 
  const open = document.getElementById("open").checked;
  if (!name || !location) {
    alert("Restaurant name and location are required");
    return;
  }
  try {
    //  Upload image IF admin selected one
    if (imageFile) {
      const formData = new FormData();
      formData.append("file", imageFile);

      const uploadRes = await fetch(`${API_BASE}/restaurants/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwt")}`
        },
        body: formData
      });

      if (!uploadRes.ok) {
        throw new Error("Image upload failed");
      }

      const uploadData = await uploadRes.json();
      imageUrl = uploadData.imageUrl;
    }
    const res = await fetch(`${API_BASE}/restaurants`, {
      method: "POST",
      headers: {
        ...authHeaders(),
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        name,
        location,
        open,
        imageUrl // may be null → backend sets default
      })
    });
    if (!res.ok) throw new Error("Forbidden");
    alert("Restaurant added");
    document.getElementById("content").innerHTML = "";
    loadRestaurants();

  } catch (err) {
    console.error(err);
    alert("Access denied or failed to add restaurant");
  }
}

function showAddMenu() {
  if (!selectedRestaurantId) {
    alert("Please select a restaurant first");
    return;
  }

  document.getElementById("content").innerHTML = `
    <div class="admin-form-card">
      <h3>Add Menu Item</h3>
      <div class="admin-form-grid">
        <label>Item name
          <input id="menuName" class="admin-input" placeholder="Item Name">
        </label>
        <label>Price
          <input id="menuPrice" class="admin-input" type="number" min="1" placeholder="Price">
        </label>
        <label>Item image
          <input type="file" id="menuImage" class="admin-file-input" accept="image/*">
        </label>
      </div>
      <div class="admin-check-row">
        <label class="admin-check">
          <input type="checkbox" id="available" checked> Available
        </label>
        <label class="admin-check">
          <input type="checkbox" id="isVeg"> Veg
        </label>
      </div>
      <button class="primary-btn admin-action-btn" onclick="addMenuItem()">Save Menu Item</button>
    </div>
  `;
}


/* function addMenu() {
  const rid = document.getElementById("rid").value;

  fetch(`${API_BASE}/restaurants/${rid}/menu`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({
      name: document.getElementById("mname").value,
      price: document.getElementById("price").value
    })
  })
  .then(res => {
    if (!res.ok) throw new Error("Forbidden");
    alert("Menu added");
  })
  .catch(() => alert("Access denied"));
} */

async function addMenuItem() {

  const name = document.getElementById("menuName").value.trim();
  const price = document.getElementById("menuPrice").value;
  const imageInput = document.getElementById("menuImage");
  const imageFile = imageInput ? imageInput.files[0] : null;
  let imageUrl = null;

  if (!name || price <= 0) {
    alert("Valid item name and price required");
    return;
  }
  if (imageFile) {
    const formData = new FormData();
    formData.append("file", imageFile);

    const uploadRes = await fetch(`${API_BASE}/restaurants/upload`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("jwt")}`
      },
      body: formData
    });

    if (!uploadRes.ok) {
      throw new Error("Menu image upload failed");
    }

    const uploadData = await uploadRes.json();
    imageUrl = uploadData.imageUrl;
  }

  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({
      name: document.getElementById("menuName").value,
      price: document.getElementById("menuPrice").value,
      available: document.getElementById("available").checked,
      isVeg: document.getElementById("isVeg").checked,
      imageUrl
    })
  })
    .then(res => {
      if (!res.ok) throw new Error();
      document.getElementById("content").innerHTML = "";
      selectRestaurant(selectedRestaurantId);
    })
    .catch(() => alert("Failed to add menu"));
}


function loadRestaurants() {
  fetch(`${API_BASE}/restaurants`, {
    headers: authHeaders()
  })
    .then(res => res.json())
    .then(restaurants => {
      const ul = document.getElementById("restaurantList");
      ul.innerHTML = "";

      restaurants.forEach(r => {
        const li = document.createElement("li");
        li.className = "admin-restaurant-item";
        const isOpen = r.open !== false;
        li.innerHTML = `
    <div class="admin-restaurant-card">
      <img 
        src="${r.imageUrl
            ? `http://localhost:8082${r.imageUrl}`
            : '/images/default-restaurant.png'
          }"
        class="admin-thumb"
      />
      <div class="admin-item-main">>
        <strong>${r.name}</strong><br/>
        <small>${r.location}</small><br/>
        <small class="${isOpen ? "admin-status-open" : "admin-status-closed"}">
          ${isOpen ? "Open" : "Closed"}
        </small>
      </div>
      <div class="admin-inline-actions">
      <button class="admin-mini-btn" onclick="event.stopPropagation(); showEditRestaurant(
  '${r.id}',
  '${r.name}',
  '${r.location}',
  '${r.imageUrl || ""}',
  ${isOpen}
)">Edit</button>

<button class="admin-mini-btn admin-danger-btn" onclick="event.stopPropagation(); deleteRestaurant('${r.id}')">
  Delete
</button>
</div>
    </div>
  `;
        li.style.cursor = "pointer";
        li.onclick = () => selectRestaurant(r.id);
        ul.appendChild(li);
      });
    });
}
function showEditRestaurant(id, name, location, imageUrl, isOpen) {
  document.getElementById("content").innerHTML = `
<div class="admin-form-card">
      <h3>Edit Restaurant</h3>
      <div class="admin-form-grid">
        <label>Restaurant name
          <input id="editRName" class="admin-input" value="${name}" />
        </label>
        <label>Location
          <input id="editLoc" class="admin-input" value="${location}" />
        </label>
        <label>Restaurant image
          <input type="file" id="editRestaurantImage" class="admin-file-input" accept="image/*">
        </label>
      </div>
      <label class="admin-check">
        <input type="checkbox" id="editOpen" ${isOpen ? "checked" : ""}> Open
      </label>
      <button class="primary-btn admin-action-btn" onclick="updateRestaurant('${id}', '${imageUrl || ""}')">
        Update Restaurant
      </button>
    </div>
  `;
}
async function updateRestaurant(id, oldImageUrl) {
  const name = document.getElementById("editRName").value.trim();
  const location = document.getElementById("editLoc").value.trim();
  const open = document.getElementById("editOpen").checked;
  const imageInput = document.getElementById("editRestaurantImage");
  const imageFile = imageInput.files[0];

  let imageUrl = oldImageUrl;

  if (!name || !location) {
    alert("Name and location required");
    return;
  }

  try {
    if (imageFile) {
      const formData = new FormData();
      formData.append("file", imageFile);

      const uploadRes = await fetch(`${API_BASE}/restaurants/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwt")}`
        },
        body: formData
      });

      const uploadData = await uploadRes.json();
      imageUrl = uploadData.imageUrl;
    }

    const res = await fetch(`${API_BASE}/restaurants/${id}`, {
      method: "PUT",
      headers: authHeaders(),
      body: JSON.stringify({ name, location, open, imageUrl })
    });

    if (!res.ok) throw new Error();

    alert("Restaurant updated");
    document.getElementById("content").innerHTML = "";
    loadRestaurants();

  } catch {
    alert("Failed to update restaurant");
  }
}

async function deleteRestaurant(restaurantId) {
  const confirm1 = confirm(
    "This will delete the restaurant and all its menu items."
  );
  if (!confirm1) return;

  const confirm2 = prompt("Type DELETE to confirm");
  if (confirm2 !== "DELETE") {
    alert("Deletion cancelled");
    return;
  }

  try {
    const res = await fetch(
      `${API_BASE}/restaurants/${restaurantId}`,
      {
        method: "DELETE",
        headers: authHeaders()
      }
    );

    if (!res.ok) throw new Error("Delete failed");

    alert("Restaurant deleted");
    loadRestaurants();

  } catch (err) {
    console.error(err);
    alert("Failed to delete restaurant");
  }
}

function selectRestaurant(restaurantId) {
  selectedRestaurantId = restaurantId;

  fetch(`${API_BASE}/restaurants/${restaurantId}`, {
    headers: authHeaders()
  })
    .then(res => res.json())
    .then(r => {
      document.getElementById("selectedRestaurantTitle").innerText =
        `Menu – ${r.name}`;

      const menuUl = document.getElementById("menuList");
      menuUl.innerHTML = "";


      /* r.menu.forEach(item => {
        const li = document.createElement("li");
        li.innerText = `${item.name} – ₹${item.price}`;
        menuUl.appendChild(li);
      }); */

      r.menu.forEach(item => {
        const li = document.createElement("li");

        li.className = "admin-menu-item";
        const isAvailable = item.available !== false;
        const isVeg = item.isVeg === true;
        li.innerHTML = `
    <img
      src="${item.imageUrl || '/images/default-food.png'}"
      class="admin-thumb admin-thumb-sm"
    />

    <div class="admin-item-main">
  <input 
  class="admin-input"
    value="${item.name}"
    onchange="updateMenuItem(
      '${item.itemId}',
      { name: this.value }
    )"
  />
  <div class="admin-item-meta">
    ${isVeg ? "Veg" : "Non-veg"} · ${isAvailable ? "Available" : "Out of stock"}
  </div>
</div>

<button class="admin-mini-btn" onclick="showEditMenuImage('${item.itemId}')">Edit</button>

    <input 
      type="number"
      class="admin-input admin-price-input"
      value="${item.price}"
      style="width:70px"
      onchange="updateMenuItem('${item.itemId}', { price: Number(this.value) })"
    />
    <label class="admin-check">
      <input
        type="checkbox"
        ${isAvailable ? "checked" : ""}
        onchange="updateMenuItem('${item.itemId}', { available: this.checked })"
      /> Available
    </label>
    <label class="admin-check">
      <input
        type="checkbox"
        ${isVeg ? "checked" : ""}
        onchange="updateMenuItem('${item.itemId}', { isVeg: this.checked })"
      /> Veg
    </label>
    <button class="admin-mini-btn admin-danger-btn" onclick="deleteMenuItem('${item.itemId}')">Delete</button>
  `;

        menuUl.appendChild(li);
      });
      //document.getElementById("addMenuSection").style.display = "block";
    });
}
function showEditMenuImage(menuId) {
  document.getElementById("content").innerHTML = `
    <div class="admin-form-card">
      <h3>Update Menu Image</h3>
      <label>Upload new image
        <input type="file" id="editMenuImage" class="admin-file-input" accept="image/*">
      </label>
      <button class="primary-btn admin-action-btn" onclick="updateMenuImage('${menuId}')">Update Image</button>
    </div>
  `;
}

async function updateMenuImage(menuId) {
  const file = document.getElementById("editMenuImage").files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  const uploadRes = await fetch(`${API_BASE}/restaurants/upload`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("jwt")}`
    },
    body: formData
  });

  const data = await uploadRes.json();

  await fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu/${menuId}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify({ imageUrl: data.imageUrl })
  });

  document.getElementById("content").innerHTML = "";
  selectRestaurant(selectedRestaurantId);
}

function updateMenuItem(menuId, updates) {
  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu/${menuId}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify(updates)
  })
    .then(res => {
      if (!res.ok) throw new Error();
    })
    .then(() => selectRestaurant(selectedRestaurantId))
    .catch(() => alert("Failed to update menu item"));
}

function deleteMenuItem(menuId) {
  if (!confirm("Delete this menu item?")) return;

  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu/${menuId}`, {
    method: "DELETE",
    headers: authHeaders()
  })
    .then(res => {
      if (!res.ok) throw new Error();
      selectRestaurant(selectedRestaurantId); // refresh
    })
    .catch(() => alert("Failed to delete menu"));
}

function showComplaintsPanel() {
  const section = document.getElementById("adminComplaintsList");
  if (section) {
    section.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}
function loadComplaintsAdmin() {
  const container = document.getElementById("adminComplaintsList");
  if (!container) return;

  fetch(`${API_BASE}/orders/complaints`, {
    headers: authHeaders()
  })
    .then(async res => {
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Failed to fetch complaints");
      }
      return res.json();
    })
    .then(complaints => {
      if (!Array.isArray(complaints) || complaints.length === 0) {
        container.innerHTML = "<p class='muted-text'>No complaints available.</p>";
        return;
      }

      container.innerHTML = complaints.map(c => `
        <div class="complaint-card">
          <div class="complaint-header">
            <strong>Complaint #${c.id} • Order ${c.orderId}</strong>
            <span class="complaint-status">${c.status}</span>
          </div>
          <p><strong>${c.subject}</strong></p>
          <p>${c.description}</p>
          <textarea id="adminResp-${c.id}" class="admin-input complaint-response-input" placeholder="Write reply...">${c.adminResponse || ""}</textarea>
          <div class="complaint-actions-row">
            <select id="status-${c.id}" class="admin-input complaint-status-select">
              <option value="IN_REVIEW" ${c.status === "IN_REVIEW" ? "selected" : ""}>IN_REVIEW</option>
              <option value="RESOLVED" ${c.status === "RESOLVED" ? "selected" : ""}>RESOLVED</option>
              <option value="CLOSED" ${c.status === "CLOSED" ? "selected" : ""}>CLOSED</option>
            </select>
            <label class="admin-check">
              <input type="checkbox" id="refund-${c.id}"> Initiate refund
            </label>
            <button class="admin-mini-btn" onclick="submitComplaintAction(${c.id})">Update</button>
          </div>
        </div>
      `).join("");
    })
    .catch(err => {
      console.error(err);
      container.innerHTML = "<p class='muted-text'>Unable to load complaints.</p>";
    });
}
function submitComplaintAction(complaintId) {
  const statusEl = document.getElementById(`status-${complaintId}`);
  const responseEl = document.getElementById(`adminResp-${complaintId}`);
  const refundEl = document.getElementById(`refund-${complaintId}`);
  if (!statusEl || !responseEl || !refundEl) return;

  fetch(`${API_BASE}/orders/complaints/${complaintId}/admin-action`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({
      status: statusEl.value,
      adminResponse: responseEl.value.trim(),
      initiateRefund: refundEl.checked
    })
  })
  .then(async res => {
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Failed to update complaint");
      }
      return res.json();
    })
    .then(() => {
      alert("Complaint updated");
      loadComplaintsAdmin();
    })
    .catch(err => {
      console.error(err);
      alert("Unable to update complaint");
    });
}
