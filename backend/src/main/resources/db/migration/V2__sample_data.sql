-- Sample catalogue and customers so the app is useful on first launch.
INSERT INTO products (sku, name, description, price, stock_quantity, reorder_level)
VALUES ('KB-MX-001', 'Mechanical Keyboard', 'Hot-swappable 75% keyboard with tactile switches', 129.00, 42, 10),
       ('MS-ERG-002', 'Ergonomic Mouse', 'Vertical wireless mouse, 6 buttons', 59.90, 8, 15),
       ('MN-27Q-003', '27" QHD Monitor', '2560x1440 IPS panel, 165Hz, USB-C', 349.00, 17, 5),
       ('HD-USB-004', 'USB-C Dock', '11-in-1 dock with dual HDMI and 100W PD', 189.50, 25, 8),
       ('CB-HDMI-005', 'HDMI 2.1 Cable (2m)', 'Braided, 48Gbps', 19.99, 120, 30),
       ('WC-4K-006', '4K Webcam', 'Auto-focus webcam with privacy shutter', 99.00, 4, 10),
       ('HS-ANC-007', 'Noise-Cancelling Headset', 'Bluetooth headset with boom mic', 159.00, 31, 10),
       ('LP-STD-008', 'Laptop Stand', 'Adjustable aluminium stand', 45.00, 60, 15);

INSERT INTO customers (name, email, phone, city)
VALUES ('Northwind Traders', 'purchasing@northwind.example', '+1 416 555 0101', 'Toronto'),
       ('Maple Leaf Studio', 'ops@mapleleaf.example', '+1 514 555 0144', 'Montreal'),
       ('Bluewater Logistics', 'it@bluewater.example', '+1 604 555 0192', 'Vancouver'),
       ('Summit Dental Group', 'admin@summitdental.example', '+1 403 555 0117', 'Calgary');
