# Get Winlogbeat
$winlogbeat_url = "https://artifacts.elastic.co/downloads/beats/winlogbeat/winlogbeat-8.3.3-windows-x86_64.zip"
$winlogbeat_download = "c:\Users\Administrator\Downloads\winlogbeat.zip"
Invoke-WebRequest -Uri $winlogbeat_url -Outfile $winlogbeat_download

# Unzip download 
Expand-Archive $winlogbeat_download -DestinationPath "c:\Program Files"

# rename directory
move "c:\Program Files\winlogbeat-8.3.3-windows-x86_64" "c:\Program Files\Winlogbeat"

# Clone Cyber repo
$cyber_repo = "https://github.com/confluentinc/cyber/archive/refs/heads/master.zip"
$repo_zip = "c:\Users\Administrator\Downloads\cyber-master.zip"
Invoke-Webrequest -Uri $cyber_repo -Outfile $repo_zip

# Unzip downloaded repo
Expand-Archive $repo_zip -DestinationPath "c:\Users\Administrator\Downloads"

# Copy winlogbeat.yml config file from repo to Program Files
copy "c:\Users\Administrator\Downloads\cyber-master\quickstart\winlogbeat\winlogbeat.yml" "c:\Program Files\Winlogbeat"
