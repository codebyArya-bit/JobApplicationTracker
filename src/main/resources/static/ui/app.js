const API = {
  list: (q, status) => {
    const params = new URLSearchParams();
    if (q && q.trim()) params.set('q', q.trim());
    if (status && status.trim()) params.set('status', status.trim());
    return fetch(`/api/apps?${params.toString()}`).then(r => {
      if (!r.ok) throw new Error(`Failed to load: ${r.status}`);
      return r.json();
    });
  },
  create: (payload) => {
    return fetch('/api/apps', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }).then(r => {
      if (!r.ok) throw new Error(`Create failed: ${r.status}`);
      return r.json();
    });
  },
  delete: (id) => {
    return fetch(`/api/apps/${id}`, { method: 'DELETE' }).then(r => {
      if (!r.ok && r.status !== 204) throw new Error(`Delete failed: ${r.status}`);
      return true;
    });
  }
};

const feedback = document.getElementById('feedback');
function showError(msg) {
  feedback.textContent = msg;
  feedback.style.display = 'block';
  feedback.className = 'error';
}
function clearError() {
  feedback.textContent = '';
  feedback.style.display = 'none';
}

function renderRows(rows) {
  const tbody = document.querySelector('#appsTable tbody');
  tbody.innerHTML = '';
  rows.forEach(a => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${a.id}</td>
      <td>${escapeHtml(a.company)}</td>
      <td>${escapeHtml(a.role)}</td>
      <td>${a.status}</td>
      <td>${a.appliedOn}</td>
      <td>${a.lastUpdate ?? ''}</td>
      <td>${escapeHtml(a.notes ?? '')}</td>
      <td class="actions">
        <button data-id="${a.id}" class="del">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function escapeHtml(s) {
  if (s == null) return '';
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

async function load() {
  clearError();
  const q = document.getElementById('q').value;
  const status = document.getElementById('status').value;
  try {
    const data = await API.list(q, status);
    renderRows(data);
  } catch (e) {
    showError(e.message || 'Failed to load applications');
  }
}

document.getElementById('filterForm').addEventListener('submit', e => {
  e.preventDefault();
  load();
});
document.getElementById('clearBtn').addEventListener('click', e => {
  document.getElementById('q').value = '';
  document.getElementById('status').value = '';
  load();
});

document.getElementById('createForm').addEventListener('submit', async e => {
  e.preventDefault();
  clearError();
  const payload = {
    company: document.getElementById('c_company').value.trim(),
    role: document.getElementById('c_role').value.trim(),
    status: document.getElementById('c_status').value,
    appliedOn: document.getElementById('c_appliedOn').value,
    notes: document.getElementById('c_notes').value.trim()
  };
  if (!payload.company || !payload.role || !payload.appliedOn) {
    showError('Company, role, and applied date are required');
    return;
  }
  try {
    await API.create(payload);
    e.target.reset();
    load();
  } catch (err) {
    showError(err.message || 'Failed to create application');
  }
});

document.querySelector('#appsTable').addEventListener('click', async e => {
  const btn = e.target.closest('button.del');
  if (!btn) return;
  const id = btn.getAttribute('data-id');
  if (!id) return;
  try {
    await API.delete(id);
    load();
  } catch (err) {
    showError(err.message || 'Failed to delete application');
  }
});

// Initial load
load();