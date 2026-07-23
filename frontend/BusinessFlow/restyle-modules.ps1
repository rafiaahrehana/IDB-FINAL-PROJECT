$base = "C:\Users\HP\OneDrive\Desktop\IDB-FINAL-PROJECT\frontend\BusinessFlow\src\app\modules"

$workspaceLabels = @{
    "servicedesk" = "SERVICE DESK"
    "support" = "SUPPORT"
    "itam" = "IT ASSETS"
    "attendance" = "ATTENDANCE"
    "platform-admin" = "PLATFORM"
    "client-portal" = "CLIENT PORTAL"
    "ai" = "AI"
    "search" = "SEARCH"
    "notifications" = "NOTIFICATIONS"
    "preferences" = "PREFERENCES"
    "roles-permissions" = "ROLES &amp; PERMISSIONS"
    "subscription" = "SUBSCRIPTION"
}

$htmlFiles = Get-ChildItem -Path $base -Filter "*.html" -Recurse |
    Where-Object {
        $rel = $_.FullName.Replace($base, "").TrimStart("\")
        $mod = $rel.Split("\")[0]
        $workspaceLabels.ContainsKey($mod)
    }

$modified = @()

foreach ($file in $htmlFiles) {
    $rel = $file.FullName.Replace($base, "").TrimStart("\")
    $module = $rel.Split("\")[0]
    $label = $workspaceLabels[$module]

    $content = Get-Content -Path $file.FullName -Raw
    $orig = $content

    # === 1. WORKSPACE LABEL + PAGE TITLE ===
    # Handle: <div class="d-flex justify-content-between..."><h4 class="mb-0">Title</h4>
    $content = $content -replace '(<div class="d-flex justify-content-between align-items-center[^"]*">\s*(?:<div[^>]*>\s*)?<h4 class="mb-0">)', "`$1`n      <div class=`"workspace-label`">$label</div>`n      "

    # Handle standalone <h4 class="mb-0">Title</h4> (not inside flex)
    # Skip if already preceded by workspace-label
    if ($content -notmatch "workspace-label") {
        $content = $content -replace '(  <h4 class="mb-0">)', "  <div class=`"workspace-label`">$label</div>`n  `$1"
    }

    # Handle <h4 class="mb-3">Title</h4> patterns
    $content = $content -replace '(<div class="d-flex justify-content-between align-items-center[^"]*">\s*<h4 class="mb-3">)', "`$1`n      <div class=`"workspace-label`">$label</div>`n      "

    # Handle <h4 class="mb-1">Subtitle patterns
    if ($content -notmatch "workspace-label") {
        $content = $content -replace '(<h4 class="mb-1[^"]*">[^<]+</h4>)', "<div class=`"workspace-label`">$label</div>`n    `$1"
    }

    # === 2. SEARCH INPUTS - wrap with input-search ===
    # Pattern: <input class="form-control form-control-sm" ... placeholder="Search...
    $content = $content -replace '(<input class="form-control form-control-sm" style="width:\s*\d+px"\s+placeholder="Search[^"]*")', '<div class="input-search me-2"><i class="bi bi-search"></i>$1></div>'

    # Pattern: <input type="text" class="form-control form-control-sm" style="width:220px" placeholder="Search...
    $content = $content -replace '(<input type="text" class="form-control form-control-sm" style="width:\d+px"\s+placeholder="Search[^"]*")', '<div class="input-search me-2"><i class="bi bi-search"></i>$1></div>'

    # === 3. MODAL HEADERS - indigo gradient ===
    $content = $content -replace '(<div class="modal-header py-2">)', '$1' -replace '(<div class="modal-header py-2">)', '<div class="modal-header py-2" style="background: linear-gradient(135deg, #1E1B4B, #4C1D95); color: #fff;">'
    $content = $content -replace '(<div class="modal-header">)', '<div class="modal-header" style="background: linear-gradient(135deg, #1E1B4B, #4C1D95); color: #fff;">'
    $content = $content -replace '(<div class="modal-header border-bottom[^"]*">)', '<div class="modal-header border-bottom py-3 px-4" style="background: linear-gradient(135deg, #1E1B4B, #4C1D95); color: #fff;">'
    $content = $content -replace '(<div class="modal-header border-top[^"]*">)', '<div class="modal-header border-top py-3 px-4" style="background: linear-gradient(135deg, #1E1B4B, #4C1D95); color: #fff;">'

    # Fix btn-close in colored headers
    $content = $content -replace '(style="background: linear-gradient\(135deg, #1E1B4B, #4C1D95\); color: #fff;">\s*<h5[^>]*>[^<]*</h5>\s*<button class="btn-close")', '$1 btn-close-white'

    # === 4. CREATE/ADD BUTTONS -> btn-success ===
    # Match btn-primary buttons with create/add/new text
    $content = $content -replace '(class="btn )btn-primary( btn-sm[^"]*"[^>]*>\s*<i class="bi bi-plus[^"]*"></i>\s*(?:New |Add |Create |Submit |Register |Start |Log Hours|Initialize Project|Add )[^<]*</button>)', '$1btn-success$2'
    $content = $content -replace '(class="btn )btn-primary( btn-sm text-nowrap"[^>]*>\s*<i class="bi bi-plus[^"]*"></i>\s*(?:Add |New |Create |Submit |Register |Start |Initialize Project)[^<]*</button>)', '$1btn-success$2'
    $content = $content -replace '(class="btn )btn-primary( btn-sm"[^>]*>\s*<i class="bi bi-plus"></i>\s*(?:New |Add |Create |Submit |Register |Start )[^<]*</button>)', '$1btn-success$2'
    # Handle buttons where text comes before icon
    $content = $content -replace '(class="btn )btn-primary( btn-sm text-nowrap"[^>]*>\s*<i class="bi bi-person-plus[^"]*"></i>\s*Add )', '$1btn-success$2'
    $content = $content -replace '(class="btn )btn-primary( btn-sm text-nowrap"[^>]*>\s*<i class="bi bi-rocket-takeoff[^"]*"></i>\s*)', '$1btn-success$2'

    # === 5. TABLE THEAD - remove table-light ===
    $content = $content -replace '<thead class="table-light">', '<thead>'

    # === 6. REMOVE INLINE CARD STYLES ===
    $content = $content -replace 'class="card border-0 shadow-sm', 'class="card'
    $content = $content -replace 'class="card shadow-sm border-0', 'class="card'
    $content = $content -replace 'card-header bg-white', 'card-header'

    # === 7. SECTION LABELS for form sections ===
    $content = $content -replace '(<div class="small fw-semibold text-secondary mb-1"><i class="bi bi-ui-checks[^"]*"></i>)([^<]+)(</div>)', '<div class="section-label">$2</div>'
    $content = $content -replace '(<div class="small fw-semibold text-secondary mb-1"><i class="bi bi-grid[^"]*"></i>)([^<]+)(</div>)', '<div class="section-label">$2</div>'

    if ($content -ne $orig) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $modified += "$module\$($file.Name)"
    }
}

Write-Host "`n=== MODIFIED FILES ===" -ForegroundColor Green
$modified | Sort-Object | ForEach-Object { Write-Host "  $_" -ForegroundColor Cyan }
Write-Host "`nTotal: $($modified.Count) files" -ForegroundColor Yellow
