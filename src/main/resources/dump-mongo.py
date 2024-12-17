import subprocess
import time
import sys
import os
import datetime

def dump_mongodb_to_file(db_host, db_name, output_path, output_file):
    try:
        # Ensure the output directory exists
        if not os.path.exists(output_path):
            os.makedirs(output_path)
            print(f"Created directory: {output_path}")

        print(f"Starting dump of database {db_name}...")
        start_time = time.time()  # Start the timer

        # Construct the full path for the output file
        full_output_file = os.path.join(output_path, output_file)

        subprocess.run(
            [
                "mongodump",
                "--uri", db_host,
                "--db", db_name,
                "--archive=" + full_output_file,
                "--gzip"
            ],
            check=True
        )

        end_time = time.time()  # End the timer
        elapsed_time = end_time - start_time  # Calculate elapsed time

        print(f"Database {db_name} dumped successfully into {full_output_file}")
        print(f"Dump completed in {elapsed_time:.2f} seconds.")
    except subprocess.CalledProcessError as e:
        print(f"Error during dump: {e}")

# Example usage

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python dumpMongo.py <db_host> <db_name> <output_path>")
        sys.exit(1)

    # Get current UTC time
    current_time_utc = datetime.datetime.now(datetime.UTC)

    # Format the time as "yyyy-MM-dd'T'HH-mm-ss"
    formatted_time = current_time_utc.strftime("%Y-%m-%dT%H-%M-%S")

    db_host = sys.argv[1]
    db_name = sys.argv[2]
    output_path = sys.argv[3]
    output_file_name = f"{formatted_time}_{db_name}_dump.archive"

    dump_mongodb_to_file(db_host, db_name, output_path, output_file_name)
