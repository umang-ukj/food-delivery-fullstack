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
    <h3>Add Restaurant</h3>
    <input id="rname" placeholder="Restaurant Name"><br><br>
    <input id="loc" placeholder="Location"><br><br>
    <input type="file" id="restaurantImage" accept="image/*">
    <label>
      <input type="checkbox" id="open"> Open
    </label><br><br>
    <button onclick="addRestaurant()">Save</button>
  `;
}

async function addRestaurant() {
  const name = document.getElementById("rname").value.trim();
  const location = document.getElementById("loc").value.trim();
  const imageInput = document.getElementById("restaurantImage");
  const imageFile = imageInput ? imageInput.files[0] : null;
  let imageUrl = null;   
  let open = true; 
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
    <h3>Add Menu Item</h3>

    <input id="menuName" placeholder="Item Name"><br><br>
    <input id="menuPrice" placeholder="Price"><br><br>
    <input type="file" id="menuImage" accept="image/*">

    <label>
      <input type="checkbox" id="available" checked> Available
    </label><br><br>

    <button onclick="addMenuItem()">Save</button>
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
      li.innerHTML = `
    <div style="display:flex; gap:12px; align-items:center;">
      <img 
        src="${
    r.imageUrl 
      ? `http://localhost:8082${r.imageUrl}` 
      : '/images/default-restaurant.png'
  }"
        style="width:90px;height:65px;object-fit:cover;border-radius:6px;"
      />
      <div>
        <strong>${r.name}</strong><br/>
        <small>${r.location}</small>
      </div>
      <button onclick="showEditRestaurant(
  '${r.id}',
  '${r.name}',
  '${r.location}',
  '${r.imageUrl || ""}'
)">Edit</button>

<button 
  style="background:#d9534f;color:white;border:none;padding:6px 10px;"
  onclick="deleteRestaurant('${r.id}')">
  Delete
</button>

    </div>
  `;
      li.style.cursor = "pointer";
      li.onclick = () => selectRestaurant(r.id);
      ul.appendChild(li);
    });
  });
}
function showEditRestaurant(id, name, location, imageUrl) {
  document.getElementById("content").innerHTML = `
    <h3>Edit Restaurant</h3>

    <input id="editRName" value="${name}" /><br><br>
    <input id="editLoc" value="${location}" /><br><br>

    <input type="file" id="editRestaurantImage" accept="image/*"><br><br>

    <button onclick="updateRestaurant('${id}', '${imageUrl || ""}')">
      Update
    </button>
  `;
}
async function updateRestaurant(id, oldImageUrl) {
  const name = document.getElementById("editRName").value.trim();
  const location = document.getElementById("editLoc").value.trim();
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
      body: JSON.stringify({ name, location, imageUrl })
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

  li.style.display = "flex";
  li.style.alignItems = "center";
  li.style.gap = "12px";

  li.innerHTML = `
    <img
      src="${item.imageUrl || '/images/default-food.png'}"
      style="
        width:70px;
        height:55px;
        object-fit:cover;
        border-radius:6px;
        border:1px solid #ccc;
      "
    />

    <div style="flex:1">
  <input 
    value="${item.name}"
    onchange="updateMenuItem(
      '${item.itemId}',
      this.value,
      ${item.price}
    )"
  />
</div>

<button onclick="showEditMenuImage('${item.itemId}')">Edit</button>

    <input 
      type="number"
      value="${item.price}"
      style="width:70px"
      onchange="updateMenuPrice('${item.itemId}', this.value)"
    />

    <button onclick="deleteMenuItem('${item.itemId}')">❌</button>
  `;

  menuUl.appendChild(li);
});
    //document.getElementById("addMenuSection").style.display = "block";
  });
}
function showEditMenuImage(menuId) {
  document.getElementById("content").innerHTML = `
    <h3>Update Menu Image</h3>
    <input type="file" id="editMenuImage" accept="image/*"><br><br>
    <button onclick="updateMenuImage('${menuId}')">Update</button>
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

function updateMenuItem(menuId, name, price) {
  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu/${menuId}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify({ name, price })
  })
  .then(res => {
    if (!res.ok) throw new Error();
  })
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

function updateMenuPrice(menuId, newPrice) {
  fetch(`${API_BASE}/restaurants/${selectedRestaurantId}/menu/${menuId}`, {
    method: "PUT",
    headers: {
      ...authHeaders(),
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      price: Number(newPrice)
    })
  })
  .then(res => {
    if (!res.ok) throw new Error();
  })
  .catch(() => alert("Failed to update price"));
}
