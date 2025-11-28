import pymongo
import subprocess
import sys
import os
import time

# Static list of collections to always include in the dump even if they don't have projectId
# Populate this with the exact collection names you want exported in full.
ALWAYS_EXPORT_COLLECTIONS = [
    "CardDescriptors",
    "EntityCrudKitSettings",
    "EntityTags",
    "ProjectDetails"
]

def export_project_collections(mongo_uri, project_id, db_name, output_path):
    """
    Exports collections containing a specific projectId, updates projectId, and removes the `_id` field.

    :param mongo_uri: MongoDB connection URI.
    :param project_id: The project ID to filter documents by.
    :param db_name: The MongoDB database name.
    :param output_path: Path to save the archive with the exported JSON files.
    """
    client = pymongo.MongoClient(mongo_uri)
    db = client[db_name]

    if not os.path.exists(output_path):
        os.makedirs(output_path)

    # List all collections in the database
    collections = db.list_collection_names()

    exported_collections = set()

    for collection_name in collections:
        if collection_name == "ReproducibleProjects":
            continue
        collection = db[collection_name]

        # Check if the collection contains `projectId`
        if collection.find_one({"projectId": project_id}):
            output_file = os.path.join(output_path, f"{collection_name}.json")
            print(f"Exporting {collection_name} to {output_file}...")

            try:
                # Use mongoexport to export the filtered data
                subprocess.run([
                    "mongoexport",
                    "--uri", mongo_uri,
                    "--db", db_name,
                    "--collection", collection_name,
                    "--query", f'{{"projectId": "{project_id}"}}',
                    "--out", output_file,
                ], check=True)

                exported_collections.add(collection_name)
            except subprocess.CalledProcessError as e:
                print(f"Error exporting collection {collection_name}: {e}")
            except Exception as e:
                print(f"Unexpected error for {collection_name}: {e}")

    # Export additional collections that should always be included (filtered by _id equal to project_id)
    for collection_name in ALWAYS_EXPORT_COLLECTIONS:
        if collection_name == "ReproducibleProjects":
            continue
        if collection_name in exported_collections:
            continue
        if collection_name not in collections:
            print(f"Skipping {collection_name}: collection does not exist in database {db_name}.")
            continue

        output_file = os.path.join(output_path, f"{collection_name}.json")
        print(f"Exporting (always) {collection_name} to {output_file}...")
        try:
            subprocess.run([
                "mongoexport",
                "--uri", mongo_uri,
                "--db", db_name,
                "--collection", collection_name,
                "--query", f'{{"_id": "{project_id}"}}',
                "--out", output_file,
            ], check=True)
        except subprocess.CalledProcessError as e:
            print(f"Error exporting collection {collection_name}: {e}")
        except Exception as e:
            print(f"Unexpected error for {collection_name}: {e}")


    print("Export completed!")


if __name__ == "__main__":
    start_time = time.time()  # Start the timer
    if len(sys.argv) != 5:
        print("Usage: python dump-project-collections.py <mongoUri> <dbName> <projectId> <output_path>")
        sys.exit(1)

    mongo_uri = sys.argv[1]
    db_name = sys.argv[2]
    project_id = sys.argv[3]
    output_path = sys.argv[4]

    export_project_collections(mongo_uri, project_id, db_name, output_path)

    end_time = time.time()  # End the timer
    elapsed_time = end_time - start_time  # Calculate elapsed time
    print(f"Dump completed in {elapsed_time:.2f} seconds.")