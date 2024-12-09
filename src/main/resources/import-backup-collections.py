import os
import json
import subprocess
import sys
import time

def replace_project_id_in_json(input_path, new_project_id):
    """
    Replaces the `projectId` in all `.json` files within the specified input directory.

    :param input_path: Path to the directory containing `.json` files.
    :param new_project_id: The new `projectId` to replace in each file.
    """
    try:
        for file_name in os.listdir(input_path):
            if file_name.endswith('.json'):
                file_path = os.path.join(input_path, file_name)
                temp_file_path = file_path + ".tmp"

                print(f"Processing file: {file_name} to replace projectId...")

                # Process the file and replace the `projectId`
                with open(file_path, 'r', encoding='utf-8') as infile, open(temp_file_path, 'w', encoding='utf-8') as outfile:
                    for line in infile:
                        document = json.loads(line)
                        document.pop('_id', None)  # Remove `_id` field
                        document['projectId'] = new_project_id # Update `projectId`
                        outfile.write(json.dumps(document) + '\n')

                # Replace the original file with the updated one
                os.replace(temp_file_path, file_path)

        print("All projectId values updated successfully!")
    except Exception as e:
        print(f"Error processing files: {e}")


def import_json_to_mongo(mongo_uri, db_name, input_path):
    """
    Imports `.json` files from the input directory into MongoDB.

    :param mongo_uri: MongoDB connection URI.
    :param db_name: MongoDB database name.
    :param input_path: Path to the directory containing `.json` files.
    """
    try:
        for file_name in os.listdir(input_path):
            if file_name.endswith('.json'):
                collection_name = os.path.splitext(file_name)[0]
                file_path = os.path.join(input_path, file_name)

                print(f"Importing {file_name} into collection {collection_name}...")

                # Use mongoimport to import the JSON file
                subprocess.run([
                    "mongoimport",
                    "--uri", mongo_uri,
                    "--db", db_name,
                    "--collection", collection_name,
                    "--file", file_path,
                    "--jsonArray"
                ], check=True)

        print("All collections imported successfully!")
    except subprocess.CalledProcessError as e:
        print(f"Error importing data: {e}")
    except Exception as e:
        print(f"Unexpected error during import: {e}")


if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python import-backup-collections.py <mongoUri> <dbName> <inputPath> <newProjectId>")
        sys.exit(1)

    mongo_uri = sys.argv[1]
    db_name = sys.argv[2]
    input_path = sys.argv[3]
    new_project_id = sys.argv[4]

    start_time = time.time()

    print("Starting to process files...")
    replace_project_id_in_json(input_path, new_project_id)
    import_json_to_mongo(mongo_uri, db_name, input_path)

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Script completed in {elapsed_time:.2f} seconds.")
