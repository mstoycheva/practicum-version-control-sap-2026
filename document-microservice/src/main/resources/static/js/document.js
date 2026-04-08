function toggleFolder(element) {
    const folderHeader = element.parentElement;
    const nestedDocs = folderHeader.nextElementSibling;
    if (nestedDocs && nestedDocs.classList.contains('nested-docs')) {
        const isVisible = nestedDocs.style.display === 'block';
        nestedDocs.style.display = isVisible ? 'none' : 'block';
    }
}
// function handleFileClick(element) {
//     const id = element.getAttribute('data-id');
//     const name = element.getAttribute('data-name');
//     const description = element.getAttribute('data-description');
//     const date = element.getAttribute('data-date');
//
//     showDetails(name, description, date, id);
//     filterAndShowDrafts(id, name, description, date);
// }

function handleFileClick(element) {
    const id = element.getAttribute('data-id');
    const name = element.innerText.trim();
    const description = element.getAttribute('data-description') || "";
    const date = element.getAttribute('data-date') || new Date().toLocaleDateString();

    showDetails(name, description, date, id);
    filterAndShowDrafts(id, name, description, date);
}

function showDetails(name, description, date, id) {
    console.log("Switching to file ID:", id);
    // const title = document.getElementById('selected-file-title');
    // if (title) title.innerText = name;
}

function handleVersion(versionId, action) {

    const method = (action === 'accept') ? 'PATCH' : 'POST';
    const url = `/document-microservice/api/versions/${versionId}${action === 'accept' ? '/accept' : '/reject'}`;

    console.log(`Sending ${method} request to: ${url}`);

    fetch(url, { method: method })
        .then(response => {
            if (response.ok) {
                const versionIndex = allDrafts.findIndex(v => String(v.id) === String(versionId));

                if (versionIndex > -1) {
                    const version = allDrafts[versionIndex];
                    const docId = version.documentId || version.docId;

                    if (action === 'accept') {
                        version.active = true;
                        version.isActive = true;
                        version.approved = true;
                        version.isApproved = true;
                    } else if (action === 'reject') {
                        allDrafts.splice(versionIndex, 1);
                    }

                    const currentDocName = document.getElementById('selected-file-title').innerText;

                    filterAndShowDrafts(docId, currentDocName, "", "");
                    console.log(`UI updated successfully for ${action}`);
                }
            } else {
                console.error(`Server error: ${response.status} for action ${action}`);
            }
        })
        .catch(err => {
            console.error("Fetch error:", err);
        });
}

function filterAndShowDrafts(selectedDocId, name, description, date) {
    const container = document.getElementById('version-container');
    const listElement = container.querySelector('.version-list');
    const activeContainer = document.getElementById('active-version-container');

    if (name) activeContainer.dataset.docName = name;
    const displayName = name || activeContainer.dataset.docName || "Document";

    const filteredVersions = allDrafts.filter(v => {
        const dId = v.documentId || v.docId;
        return String(dId) === String(selectedDocId);
    });

    const activeVersions = filteredVersions.filter(v => {
        const vNum = v.versionNumber || v.version_number;
        return (vNum === 1 || v.active || v.isActive || v.approved || v.isApproved);
    }).sort((a, b) => (b.versionNumber || b.version_number) - (a.versionNumber || a.version_number));

    if (activeContainer) {
        if (activeVersions.length > 0) {
            activeContainer.innerHTML = activeVersions.map((v, index) => {
                const isLatest = index === 0;
                const vNum = v.versionNumber || v.version_number;

                return `
                <div class="version-item" style="display: flex; justify-content: space-between; align-items: center; padding: 12px; border: 1px solid #eee; border-radius: 8px; border-left: 5px solid ${isLatest ? '#569e68' : '#ccc'}; background: ${isLatest ? '#fafafa' : 'transparent'}; margin-bottom: 10px;">
                    <div class="file-info" style="display: flex; align-items: center; gap: 12px;">
                        <img src="${iconDocPath}" alt="doc" style="width: 20px;">
                        <div>
                            <strong style="display: block; font-size: 0.9rem;">${displayName} (v.${vNum}) ${isLatest ? '<span style="color: #569e68; font-size: 0.7rem;">[CURRENT]</span>' : ''}</strong>
                            <small style="color: #888;">${v.message || 'Approved Version'}</small>
                        </div>
                    </div>
                    <div style="text-align: right;">
                        <a href="/document-microservice/documents/download/${v.id}">
                           <img src="${iconDownloadPath}" alt="download" style="width: 25px; height: 25px;">
                        </a>
                    </div>
                </div>`;
            }).join('');
        } else {
            activeContainer.innerHTML = '<p style="text-align: center; color: #888;">No active versions.</p>';
        }
    }

    const draftsOnly = filteredVersions.filter(v => {
        const vNum = v.versionNumber || v.version_number;
        const isApproved = (v.active || v.isActive || v.approved || v.isApproved);
        return vNum !== 1 && !isApproved;
    });

    if (container) {
        container.style.display = 'block';
        if (draftsOnly.length > 0) {
            listElement.innerHTML = draftsOnly.map(draft => `
                <li class="version-item" style="display: flex; align-items: center; justify-content: space-between; padding: 8px 10px; background: white; border-radius: 8px; margin-bottom: 6px; border: 1px solid #eee;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <img src="${iconDocPath}" alt="doc" style="width: 18px;">
                        <span style="font-size: 10px; background: #f0f0f0; padding: 1px 5px; border-radius: 4px;">v.${draft.versionNumber || draft.version_number}</span>
                        <span style="font-size: 11px;">${draft.message || 'Draft'}</span>
                    </div>
                    <div style="display: flex; gap: 5px;">
                        <button onclick="handleVersion('${draft.id}', 'accept')" 
                                style="padding: 2px 8px; border: none; background: #569e68; color: white; border-radius: 4px; cursor: pointer;">✔</button>
                        <button onclick="handleVersion('${draft.id}', 'reject')" 
                                style="padding: 2px 8px; border: none; background: #e84c4c; color: white; border-radius: 4px; cursor: pointer;">✖</button>
                    </div>
                </li>`).join('');
        } else {
            listElement.innerHTML = '<p style="font-size: 0.85rem; color: #888; padding-left: 10px;">No pending drafts.</p>';
        }
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
