document.addEventListener('DOMContentLoaded', () => {

    // === 1. Input Methods (File Upload vs Paste Text) ===
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');
    const uploadInput = document.getElementById('resumeFileInput');
    const pasteTextarea = document.getElementById('resumeTextarea');
    const inputMethodField = document.getElementById('inputMethod');

    if (tabBtns.length > 0) {
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTab = btn.getAttribute('data-tab');
                
                tabBtns.forEach(b => b.classList.remove('active'));
                tabPanels.forEach(p => p.classList.remove('active'));
                
                btn.classList.add('active');
                const targetPanel = document.getElementById(`${targetTab}Panel`);
                if (targetPanel) {
                    targetPanel.classList.add('active');
                }

                if (inputMethodField) {
                    inputMethodField.value = targetTab;
                }

                // Reset validations/required attributes based on active tab
                if (targetTab === 'upload') {
                    if (uploadInput) uploadInput.required = true;
                    if (pasteTextarea) {
                        pasteTextarea.required = false;
                        pasteTextarea.value = '';
                    }
                } else {
                    if (uploadInput) {
                        uploadInput.required = false;
                        uploadInput.value = '';
                    }
                    if (pasteTextarea) pasteTextarea.required = true;
                    const fileStatus = document.getElementById('fileStatus');
                    if (fileStatus) fileStatus.style.display = 'none';
                }
            });
        });
    }

    // === 2. Drag & Drop File Upload Interactivity ===
    const uploadZone = document.getElementById('uploadZone');
    const fileStatus = document.getElementById('fileStatus');
    const fileNameText = document.getElementById('fileName');

    if (uploadZone && uploadInput) {
        ['dragenter', 'dragover'].forEach(eventName => {
            uploadZone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                uploadZone.classList.add('dragover');
            }, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            uploadZone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                uploadZone.classList.remove('dragover');
            }, false);
        });

        uploadZone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            const files = dt.files;
            if (files.length > 0) {
                uploadInput.files = files;
                updateFileStatus(files[0].name);
            }
        });

        uploadInput.addEventListener('change', (e) => {
            if (uploadInput.files.length > 0) {
                updateFileStatus(uploadInput.files[0].name);
            }
        });
    }

    function updateFileStatus(name) {
        if (fileStatus && fileNameText) {
            fileNameText.textContent = name;
            fileStatus.style.display = 'flex';
        }
    }

    // === 3. Main Form Submission & High-Tech Loader ===
    const analyzerForm = document.getElementById('analyzerForm');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const subtextLoader = document.getElementById('subtextLoader');

    const loadingStages = [
        "Initializing secure parser module...",
        "Extracting resume file contents...",
        "Decoding layout syntax and formatting...",
        "Parsing sections and educational indices...",
        "Extracting candidate contact profiles...",
        "Searching for GitHub & LinkedIn linkages...",
        "Indexing skill keywords from Job Description...",
        "Executing term frequency analysis...",
        "Compiling comparative keyword metrics...",
        "Generating final ATS ranking output..."
    ];

    if (analyzerForm) {
        analyzerForm.addEventListener('submit', (e) => {
            // Basic custom verification
            const activeMethod = inputMethodField ? inputMethodField.value : 'upload';
            if (activeMethod === 'upload' && (!uploadInput || uploadInput.files.length === 0)) {
                alert("Please select a resume file to upload.");
                e.preventDefault();
                return;
            } else if (activeMethod === 'paste' && (!pasteTextarea || pasteTextarea.value.trim() === '')) {
                alert("Please paste your resume text content.");
                e.preventDefault();
                return;
            }

            // Show loading screen
            if (loadingOverlay) {
                loadingOverlay.style.display = 'flex';
                
                let stageIndex = 0;
                const intervalId = setInterval(() => {
                    if (subtextLoader && stageIndex < loadingStages.length) {
                        subtextLoader.textContent = loadingStages[stageIndex];
                        stageIndex++;
                    } else {
                        stageIndex = 0; // Loop loading stages if it takes longer
                    }
                }, 1200);

                // Let form submit normally
            }
        });
    }

    // === 4. Score Animations & Charts (Triggered on results view load) ===
    const scoreContainer = document.getElementById('atsScoreValue');
    if (scoreContainer) {
        const targetScore = parseInt(scoreContainer.getAttribute('data-score'), 10) || 0;
        
        // 4a. Animate Circular SVG Gauge
        const gaugeFill = document.querySelector('.gauge-fill');
        if (gaugeFill) {
            const radius = 90;
            const circumference = 2 * Math.PI * radius; // ~565.48
            
            // Calculate stroke dash offset
            const offset = circumference - (targetScore / 100) * circumference;
            
            // Trigger animation after brief delay for smooth effect
            setTimeout(() => {
                gaugeFill.style.strokeDashoffset = offset;
            }, 300);
        }

        // 4b. Animate Score Text Counting Up
        const numberDisplay = document.getElementById('atsNumberDisplay');
        if (numberDisplay) {
            let count = 0;
            const duration = 1800; // ms
            const stepTime = Math.max(Math.floor(duration / targetScore), 12);
            
            setTimeout(() => {
                const timer = setInterval(() => {
                    count++;
                    numberDisplay.textContent = count;
                    if (count >= targetScore) {
                        clearInterval(timer);
                        numberDisplay.textContent = targetScore;
                    }
                }, stepTime);
            }, 300);
        }

        // 4c. Animate Horizontal Sub-metric progress bars
        const progressBars = document.querySelectorAll('.metric-bar-fill');
        progressBars.forEach(bar => {
            const targetVal = bar.getAttribute('data-value') || '0';
            setTimeout(() => {
                bar.style.width = `${targetVal}%`;
                
                // Colorize progress bar based on performance
                const scoreInt = parseInt(targetVal, 10);
                if (scoreInt >= 80) {
                    bar.style.background = 'linear-gradient(90deg, #10b981, #059669)';
                    bar.style.boxShadow = '0 0 10px rgba(16, 185, 129, 0.4)';
                } else if (scoreInt >= 50) {
                    bar.style.background = 'linear-gradient(90deg, #f59e0b, #d97706)';
                    bar.style.boxShadow = '0 0 10px rgba(245, 158, 11, 0.4)';
                } else {
                    bar.style.background = 'linear-gradient(90deg, #ef4444, #dc2626)';
                    bar.style.boxShadow = '0 0 10px rgba(239, 68, 68, 0.4)';
                }
            }, 600);
        });

        // 4d. Dynamic scroll to results on form post completion
        const dashboard = document.getElementById('analysisDashboard');
        if (dashboard) {
            dashboard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    // === 5. Tabbed Navigation inside Results Dashboard ===
    const resTabBtns = document.querySelectorAll('.results-tab-btn');
    const resPanels = document.querySelectorAll('.results-panel');

    if (resTabBtns.length > 0) {
        resTabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetPanelId = btn.getAttribute('data-panel');
                
                resTabBtns.forEach(b => b.classList.remove('active'));
                resPanels.forEach(p => p.classList.remove('active'));
                
                btn.classList.add('active');
                const targetPanel = document.getElementById(targetPanelId);
                if (targetPanel) {
                    targetPanel.classList.add('active');
                }
            });
        });
    }
});
