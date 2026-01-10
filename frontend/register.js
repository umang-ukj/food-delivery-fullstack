document.getElementById("registerForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const msg = document.getElementById("msg");
  const btn = document.getElementById("registerBtn");

  msg.style.display = "none";
  msg.className = "error-text";

  if (!email || !password) {
    showError("Email and password are required");
    return;
  }

  if (!email.includes("@")) {
    showError("Invalid email format");
    return;
  }
  if (password.length < 6) {
    showError("Password must be at least 6 characters");
    return;
  }

  btn.disabled = true;
  btn.innerText = "Registering...";

  try {
    const response = await fetch("http://localhost:8080/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      throw new Error("Registration failed");
    }

    msg.innerText = "Registration successful. Redirecting to login...";
    msg.style.display = "block";
    msg.style.color = "green";

    setTimeout(() => {
      window.location.href = "login.html";
    }, 1200);

  } 
  catch (err) {
   showError(err.message || "Registration failed");
     } finally {
      btn.disabled = false;
      btn.innerText = "Register";
    }

  function showError(text) {
    msg.innerText = text;
    msg.style.display = "block";
    msg.style.color = "red";
  }
});
