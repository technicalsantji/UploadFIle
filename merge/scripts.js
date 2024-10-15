const CHUNK_SIZE = 1024; // 1 MB chunks

function uploadFile() {
    const file = document.getElementById('fileInput').files[0];
    if (!file) return;
    const progressContainer = document.querySelector('.progress-container');
    progressContainer.classList.add('visible');  // Show the progress bar

    let start = 0;
    let chunkIndex = 0;
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);

    const progressBarFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    const uploadStatus = document.getElementById('uploadStatus');
    const progressBarContainer = document.querySelector('.progress-container');
    progressBarContainer.style="hidden:false"
    // Reset progress bar for new upload
    progressBarFill.style.width = '0%';
    progressText.textContent = '0%';
    uploadStatus.textContent = '';
    progressBarContainer.classList.remove('hidden');
    uploadStatus.classList.remove('hidden');

    function uploadNextChunk() {
        if (start >= file.size) {
            notifyMerge(file.name); // Merge chunks
            return;
        }

        const chunk = file.slice(start, start + CHUNK_SIZE);
        const formData = new FormData();
        formData.append('file', chunk);
        formData.append('fileName', file.name);
        formData.append('chunkIndex', chunkIndex);
        formData.append('totalChunks', totalChunks);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'http://localhost:4594/upload-chunk', true);

        xhr.upload.onprogress = function (event) {
            if (event.lengthComputable) {
                const percentComplete = ((chunkIndex + 1) / totalChunks) * 100;
                console.log(percentComplete);
                updateProgressBar(percentComplete);
            }
        };

        xhr.onload = function () {
            if (xhr.status === 200) {
                chunkIndex++;
                start += CHUNK_SIZE;
                uploadNextChunk(); // Upload next chunk
            } else {
                console.error("Error uploading chunk " + chunkIndex);
            }
        };

        xhr.send(formData);
    }

    function updateProgressBar(percentage) {
        progressBarFill.style.width = percentage + '%';
        progressText.textContent = percentage.toFixed(2) + '%';
    }

    uploadNextChunk(); // Start uploading

    function notifyMerge(fileName) {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'http://localhost:4594/merge-chunks', true);
        xhr.setRequestHeader('Content-Type', 'application/json');

        xhr.onload = function () {
            if (xhr.status === 200) {
                // Ensure progress bar reaches 100%
                updateProgressBar(100);

                // Show success message
                uploadStatus.textContent = 'File merged successfully!';
                uploadStatus.classList.add('visible');

                // Remove progress bar after delay
                setTimeout(() => {
                    progressBarContainer.classList.add('hidden'); // Hide progress bar
                    uploadStatus.classList.add('hidden'); // Hide status message
                }, 1500); // Delay of 1.5 seconds
            } else {
                uploadStatus.textContent = 'Error merging file.';
                uploadStatus.style.color = 'red';
            }
        };

        xhr.send(JSON.stringify({ fileName }));
    }
}
