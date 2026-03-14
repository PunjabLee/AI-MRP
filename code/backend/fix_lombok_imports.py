import os
import re

# Find all Java files
java_files = []
for root, dirs, files in os.walk('.'):
    for file in files:
        if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

# Process each file
for filepath in java_files:
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    # Check if file has @Data but not import lombok.Data
    if '@Data' in content and 'import lombok.Data' not in content:
        # Check if it has any lombok import
        if 'import lombok.' in content:
            # Add import after first lombok import
            content = re.sub(
                r'(import lombok\.[^;]+;)',
                r'\1\nimport lombok.Data;',
                content,
                count=1
            )

            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed: {filepath}")

print(f"Processed {len(java_files)} files")
