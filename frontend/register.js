document.getElementById("registerForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

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

    document.getElementById("msg").innerText =
      "Registration successful. Please login.";

    setTimeout(() => {
      window.location.href = "login.html";
    }, 1200);

  } catch (err) {
    document.getElementById("msg").innerText = err.message;
  }
});
