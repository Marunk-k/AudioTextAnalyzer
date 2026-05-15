function pollStatus(projectId) {
  const el = document.getElementById('statusBadge');
  if (!el) return;
  setInterval(() => {
    fetch(`/projects/${projectId}/status`)
      .then(r => r.json())
      .then(d => {
        el.innerText = d.statusLabel || d.status;
      });
  }, 3000);
}
