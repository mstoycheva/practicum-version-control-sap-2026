function toggleFolder(element) {
    const folderHeader = element.parentElement;
    const nestedDocs = folderHeader.nextElementSibling;
    if (nestedDocs && nestedDocs.classList.contains('nested-docs')) {
        const isVisible = nestedDocs.style.display === 'block';
        nestedDocs.style.display = isVisible ? 'none' : 'block';
    }
}
function handleFileClick(element) {
    const id = element.getAttribute('data-id');
    const name = element.getAttribute('data-name');
    const description = element.getAttribute('data-description');
    const date = element.getAttribute('data-date');

    showDetails(name, description, date, id);
    filterAndShowDrafts(id, name, description, date);
}

function showDetails(name, description, date, id) {
    // console.log("Switching to file ID:", id);
    const title = document.getElementById('selected-file-title');
    if (title) title.innerText = name;
}

function filterAndShowDrafts(selectedDocId, name, description, date) {
    const activeContainer = document.getElementById('active-version-container');
    const container = document.getElementById('version-container');
    const listElement = container.querySelector('.version-list');

    const filtered = allDrafts.filter(v => String(v.documentId || v.docId) === String(selectedDocId));

    if (filtered.length === 0) {
        if (activeContainer) activeContainer.innerHTML = '<p style="text-align: center; color: #888;">No files found.</p>';
        if (container) container.style.display = 'none';
        return;
    }

    const activeVersions = filtered.filter(v => {
        const vNum = v.versionNumber || v.version_number;
        const isApproved = v.isActive === true || v.isApproved === true;
        return (vNum === 1 || isApproved);
    }).sort((a, b) => (b.versionNumber || 0) - (a.versionNumber || 0));

    const draftsOnly = filtered.filter(v => {
        const vNum = v.versionNumber || v.version_number;
        const isApproved = v.isActive === true || v.isApproved === true;
        return vNum !== 1 && !isApproved;
    });

    if (activeContainer) {
        activeContainer.innerHTML = activeVersions.map((v, idx) => `
            <div class="version-item" style="display: flex; justify-content: space-between; align-items: center; padding: 15px; border: 1px solid #eee; border-radius: 8px; border-left: 5px solid ${idx === 0 ? '#569e68' : '#ccc'}; background: #fafafa; margin-bottom: 10px;">
                <div>
                    <strong>${name} (v.${v.versionNumber || v.version_number})</strong>
                    <div style="font-size: 0.8rem; color: #888;">${v.message || 'Approved'} | ${date}</div>
                </div>
                <a href="/document-microservice/documents/download/${v.id}">
                    <img src="${typeof iconDownloadPath !== 'undefined' ? iconDownloadPath : ''}" style="width: 25px;">
                </a>
            </div>
        `).join('');
    }

    if (container) {
        container.style.display = 'block';
        listElement.innerHTML = draftsOnly.length > 0 ? draftsOnly.map(d => `
            <li class="version-item" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: white; border: 1px solid #eee; border-radius: 8px; margin-bottom: 6px;">
                <span style="font-size: 11px;">v.${d.versionNumber || d.version_number} - ${d.message || 'Draft'}</span>
                <div style="display: flex; gap: 4px;">
                    <button onclick="handleVersion('${d.id}', 'accept')" style="background: #569e68; color: white; border: none; padding: 2px 8px; border-radius: 4px; cursor: pointer;">✔</button>
                    <button onclick="handleVersion('${d.id}', 'reject')" style="background: #e84c4c; color: white; border: none; padding: 2px 8px; border-radius: 4px; cursor: pointer;">✖</button>
                </div>
            </li>
        `).join('') : '<p style="font-size: 0.85rem; color: #888; padding: 10px;">No drafts available.</p>';
    }
}

function showProjectFields() {
    const fields = document.getElementById('projectFields');
    const mainBtn = document.getElementById('toggleProjectBtn');
    if (fields.style.display === 'flex') {
        fields.style.display = 'none';
        mainBtn.innerHTML = '<span>NEW PROJECT</span>';

        mainBtn.style.backgroundColor = '#90B69F';
        mainBtn.style.color = '#000';
        mainBtn.style.border = '1px solid #90B69F';
    } else {
        fields.style.display = 'flex';
        mainBtn.innerHTML = '<span>Cancel</span>';
        mainBtn.style.backgroundColor = '#f3caca';
        mainBtn.style.color = '#b31c1c';
        mainBtn.style.border = '1px solid #b31c1c';
    }
}

function handleVersion(versionId, action) {
    fetch(`/document-microservice/api/versions/${versionId}/${action}`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            console.log("OK");

            if (action === 'accept') {
                const acceptedVersion = allDrafts.find(v => v.id === versionId);
                if (acceptedVersion) {
                    const docId = acceptedVersion.documentId || acceptedVersion.docId;

                    allDrafts.forEach(v => {
                        const currentDocId = v.documentId || v.docId;
                        if (String(currentDocId) === String(docId)) {
                            v.isActive = (v.id === versionId);
                        }
                    });

                    filterAndShowDrafts(
                        docId,
                        acceptedVersion.message,
                        acceptedVersion.message,
                        new Date().toLocaleDateString()
                    );
                }
            } else {
                const index = allDrafts.findIndex(v => v.id === versionId);
                if (index > -1) {
                    const docId = allDrafts[index].documentId || allDrafts[index].docId;
                    allDrafts.splice(index, 1);
                    filterAndShowDrafts(docId, "", "", "");
                }
            }
        }
    }).catch(err => console.error("Error:", err));
}