import os
import json
import subprocess
import sys
import time
import uuid
from bson import ObjectId


# Collections that should NOT have `projectId` replaced during import.
# For these collections we will only handle `_id` regeneration/removal as before.
STATIC_COLLECTIONS_NO_PROJECTID = [
    "CardDescriptors",
    "EntityCrudKitSettings",
    "EntityTags",
]


def is_uuid4(value):
    """
    Validates if a given value is a UUIDv4 string.
    """
    try:
        uuid_obj = uuid.UUID(value)
        return uuid_obj.version == 4
    except (ValueError, TypeError):
        return False


def replace_project_id_in_json(input_path, new_project_id):
    """
    Replaces the `projectId` in all `.json` files within the specified input directory.
    Handles `_id`: overwrites if it's a valid UUIDv4 (string), or removes if it's an ObjectId or invalid.
    """
    try:
        for file_name in os.listdir(input_path):
            if file_name.endswith('.json'):
                file_path = os.path.join(input_path, file_name)
                temp_file_path = file_path + ".tmp"
                collection_name = os.path.splitext(file_name)[0]

                print(f"Processing file: {file_name} to replace projectId...")

                # Process the file and replace the `projectId`
                with open(file_path, 'r', encoding='utf-8') as infile, open(temp_file_path, 'w', encoding='utf-8') as outfile:
                    for line in infile:
                        document = json.loads(line)
                        # For collections in the static list: set `_id` to new_project_id and do NOT touch projectId
                        if collection_name in STATIC_COLLECTIONS_NO_PROJECTID:
                            document['_id'] = new_project_id
                        else:
                            # Existing flow: handle `_id` and set `projectId`
                            if '_id' in document:
                                if isinstance(document['_id'], str) and is_uuid4(document['_id']):
                                    # If _id is a valid UUIDv4 string, overwrite with a new UUIDv4
                                    document['_id'] = str(uuid.uuid4())
                                else:
                                    # If _id is not a valid UUIDv4 or is an ObjectId, remove it
                                    del document['_id']

                            document['projectId'] = new_project_id  # Update `projectId`
                        outfile.write(json.dumps(document) + '\n')
                # Replace the original file with the updated one
                os.replace(temp_file_path, file_path)

        print("All projectId values updated successfully!")
    except Exception as e:
        print(f"Error processing files: {e}")


def import_json_to_mongo(mongo_uri, db_name, input_path):
    """
    Imports `.json` files from the input directory into MongoDB in batches.

    :param mongo_uri: MongoDB connection URI.
    :param db_name: MongoDB database name.
    :param input_path: Path to the directory containing `.json` files.
    """
    batch_size = int(os.getenv('WEBPROTEGE_IMPORT_BATCH_SIZE', 1500))  # Default to 1500 if not set
    delay = float(os.getenv('WEBPROTEGE_IMPORT_DELAY', 0.3))  # Default to 0.3 seconds if not set


    try:
        for file_name in os.listdir(input_path):
            if file_name.endswith('.json'):
                collection_name = os.path.splitext(file_name)[0]
                file_path = os.path.join(input_path, file_name)

                print(f"Importing {file_name} into collection {collection_name} in batches...")

                with open(file_path, 'r', encoding='utf-8') as infile:
                    batch = []
                    for line_number, line in enumerate(infile, start=1):
                        batch.append(json.loads(line))

                        # Process a batch when the size reaches `batch_size`
                        if len(batch) == batch_size:
                            import_batch(batch, mongo_uri, db_name, collection_name)
                            batch = []
                            time.sleep(delay)  # Wait for `delay` seconds before the next batch

                    # Import any remaining documents in the final batch
                    if batch:
                        import_batch(batch, mongo_uri, db_name, collection_name)

        print("All collections imported successfully!")
    except Exception as e:
        print(f"Unexpected error during import: {e}")


def import_batch(batch, mongo_uri, db_name, collection_name):
    """
    Imports a single batch of documents into MongoDB.

    :param batch: A list of documents to import.
    :param mongo_uri: MongoDB connection URI.
    :param db_name: MongoDB database name.
    :param collection_name: Name of the target collection.
    """
    try:
        temp_batch_file = f"{collection_name}_batch.json"
        with open(temp_batch_file, 'w', encoding='utf-8') as batch_file:
            for doc in batch:
                batch_file.write(json.dumps(doc) + '\n')

        subprocess.run([
            "mongoimport",
            "--uri", mongo_uri,
            "--db", db_name,
            "--collection", collection_name,
            "--file", temp_batch_file
        ], check=True)

        os.remove(temp_batch_file)  # Clean up temporary batch file
        print(f"Imported batch into collection {collection_name}")
    except subprocess.CalledProcessError as e:
        print(f"Error importing batch: {e}")
    except Exception as e:
        print(f"Unexpected error during batch import: {e}")


if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python import_project.py <mongoUri> <dbName> <inputPath> <newProjectId>")
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
