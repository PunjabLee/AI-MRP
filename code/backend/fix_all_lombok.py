import os
import re

# Find all Java files
java_files = []
for root, dirs, files in os.walk('.'):
    for file in files:
        if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

# All Lombok annotations that need imports
lombok_annotations = [
    'Data', 'Builder', 'AllArgsConstructor', 'RequiredArgsConstructor',
    'Getter', 'Setter', 'Slf4j', 'Log', 'NoArgsConstructor'
]

# Process each file
for filepath in java_files:
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    modified = False
    original_content = content

    for ann in lombok_annotations:
        # Check if file has @ann but not import lombok.ann
        if f'@{ann}' in content and f'import lombok.{ann}' not in content:
            # Check if it has any lombok import
            if 'import lombok.' in content:
                # Add import after first lombok import
                content = re.sub(
                    r'(import lombok\.[^;]+;)',
                    rf'\1\nimport lombok.{ann};',
                    content,
                    count=1
                )
                modified = True

    if modified and content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {filepath}")

print(f"Processed {len(java_files)} files")
