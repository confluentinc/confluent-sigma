# Source URL
$url = "https://download.sysinternals.com/files/Sysmon.zip"

# Destation file
$dest = "c:\Users\Administrator\Downloads\Sysmon.zip"

# Download Sysmon.zip
Invoke-WebRequest -Uri $url -OutFile $dest 

# Unzip Sysmon.zip
Expand-Archive $dest -DestinationPath "c:\Users\Administrator\Downloads\Sysmon\" 


# Download sysmonconfig.xml - TODO: replace Bert's Gist with Confluent Cyber repo
$sysmonconfig_gist = "https://gist.github.com/berthayes/7a709e6e2e3793ecf7db73c962786767/archive/e9f812c398a74ee97a9908d351138424d1463a76.zip"
$local_sysmon_config = "c:\Users\Administrator\Downloads\Sysmon\sysmonconfig_gist.zip"

Invoke-WebRequest -Uri $sysmonconfig_gist -Outfile $local_sysmon_config

Expand-Archive $local_sysmon_config -DestinationPath "c:\Users\Administrator\Downloads\Sysmon"

copy "c:\Users\Administrator\Downloads\Sysmon\7a709e6e2e3793ecf7db73c962786767-e9f812c398a74ee97a9908d351138424d1463a76\sysmonconfig.xml" "c:\Users\Administrator\Downloads\Sysmon"

Start-Process -FilePath "c:\Users\Administrator\Downloads\Sysmon\Sysmon64.exe" -ArgumentList  "-i c:\Users\Administrator\Downloads\Sysmon\sysmonconfig.xml -accepteula" -Verb RunAs
