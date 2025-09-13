if [ -z "$1" ]; then
  echo "-> Please provide a file name"
  exit 1
fi

# Default folder and file name
folder_name='db/migrations/'
file_name="$1"

# Process optional flags
while getopts ":s" opt; do
  case ${opt} in
    s)
      folder_name='db/seeds/'
      ;;
    *)
      echo "-> Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done

# Generate timestamp
time_stamp=$(date +%s)

# Construct file path
file_path="${folder_name}V${time_stamp}__${file_name}.sql"

# Create the file
touch "$file_path"

echo "-> SQL file created at: $file_path"
