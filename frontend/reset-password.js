const API_BASE = "http://localhost:8080";

(function prefillEmailFromQuery() {
  const params = new URLSearchParams(window.location.search);
  const email = params.get("email") || "";
  const emailInput = document.getElementById("resetEmail");

  if (emailInput && email) {
    emailInput.value = decodeURIComponent(email);
  }
})();

function submitPasswordReset() {
  const email = document.getElementById("resetEmail")?.value.trim();
  const temporaryPassword = document.getElementById("tempPassword")?.value;
  const newPassword = document.getElementById("newPassword")?.value;
  const confirmPassword = document.getElementById("confirmPassword")?.value;

  const msg = document.getElementById("resetMsg");
  const btn = document.getElementById("resetBtn");

  msg.style.display = "none";
  msg.style.color = "red";

  if (!email || !temporaryPassword || !newPassword || !confirmPassword) {
    showError("All fields are required");
    return;
  }

  if (!email.includes("@")) {
    showError("Invalid email format");
    return;
  }

  if (newPassword.length < 6) {
    showError("Password must be at least 6 characters");
    return;
  }

  if (newPassword !== confirmPassword) {
    showError("New password and confirm password must match");
    return;
  }

  btn.disabled = true;
  btn.innerText = "Updating...";

  fetch(`${API_BASE}/auth/reset-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      email,
      temporaryPassword,
      newPassword,
      confirmPassword
    })
  })
    .then(async (res) => {
      const text = await res.text();
      if (!res.ok) throw new Error(text || "Failed to reset password");
      return text;
    })
    .then((message) => {
      msg.innerText = message || "Password reset successful";
      msg.style.color = "green";
      msg.style.display = "block";

      setTimeout(() => {
        window.location.href = "login.html";
      }, 1200);
    })
    .catch((err) => {
      showError(err.message || "Failed to reset password");
    })
    .finally(() => {
      btn.disabled = false;
      btn.innerText = "Set New Password";
    });

  function showError(text) {
    msg.innerText = text;
    msg.style.display = "block";
    msg.style.color = "red";
  }
}
