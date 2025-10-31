(() => {
  const form = document.getElementById('fetchForm');
  const result = document.getElementById('result');
  const clearBtn = document.getElementById('clearBtn');

  clearBtn.addEventListener('click', () => {
    document.getElementById('resourceId').value = 'ee03643a-ee4c-48c2-ac30-9f2ff26ab722';
    document.getElementById('apiKey').value = '';
    document.getElementById('format').value = 'json';
    document.getElementById('offset').value = '';
    document.getElementById('limit').value = '';
    document.getElementById('stateName').value = '';
    document.getElementById('finYear').value = '';
    result.textContent = 'Cleared.';
  });

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();

    const resourceId = document.getElementById('resourceId').value.trim();
    const apiKey = document.getElementById('apiKey').value.trim();
    const format = document.getElementById('format').value;
    const offset = document.getElementById('offset').value;
    const limit = document.getElementById('limit').value;
    const stateName = document.getElementById('stateName').value.trim();
    const finYear = document.getElementById('finYear').value.trim();

    if (!resourceId) {
      result.textContent = 'Resource ID is required.';
      return;
    }

    if (!apiKey) {
      result.textContent = 'API key is required. Check docker-compose.yml or application.yml for the configured value.';
      return;
    }

    const params = new URLSearchParams();
    params.set('api-key', apiKey);
    if (format) params.set('format', format);
    if (offset) params.set('offset', offset);
    if (limit) params.set('limit', limit);
    if (stateName) params.set('filters[state_name]', stateName);
    if (finYear) params.set('filters[fin_year]', finYear);

    const url = `/resource/${encodeURIComponent(resourceId)}?${params.toString()}`;
    result.textContent = `Fetching ${url} ...`;

    try {
      const resp = await fetch(url, { method: 'GET' });
      const contentType = resp.headers.get('content-type') || '';

      if (!resp.ok) {
        const text = await resp.text();
        result.textContent = `Error ${resp.status} ${resp.statusText}\n\n${text}`;
        return;
      }

      if (contentType.includes('application/json')) {
        const data = await resp.json();
        result.textContent = JSON.stringify(data, null, 2);
      } else {
        const text = await resp.text();
        result.textContent = text;
      }
    } catch (err) {
      result.textContent = `Request failed: ${err}`;
    }
  });
})();
