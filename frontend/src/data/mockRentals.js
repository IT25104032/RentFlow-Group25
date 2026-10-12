export const mockRentals = [
    {
        rentalId: 1,
        companyName: "ABC Equipment Rentals",
        customerName: "Nimal Perera",
        customerPhone: "0771234567",
        rentalDate: "2026-09-20",
        startDate: "2026-09-20",
        dueDate: "2026-09-27",
        rentalStatus: "ACTIVE",

        items: [
            {
                rentalItemId: 1,
                equipmentName: "Angle Grinder",
                itemCode: "AG-001",
                quantityRented: 3,
                quantityAlreadyReturned: 0
            },
            {
                rentalItemId: 2,
                equipmentName: "Electric Drill",
                itemCode: "ED-001",
                quantityRented: 2,
                quantityAlreadyReturned: 1
            }
        ]
    },

    {
        rentalId: 2,
        companyName: "ABC Equipment Rentals",
        customerName: "Kamal Silva",
        customerPhone: "0712345678",
        rentalDate: "2026-09-22",
        startDate: "2026-09-22",
        dueDate: "2026-09-29",
        rentalStatus: "ACTIVE",

        items: [
            {
                rentalItemId: 3,
                equipmentName: "Pressure Washer",
                itemCode: "PW-001",
                quantityRented: 1,
                quantityAlreadyReturned: 0
            }
        ]
    },

    {
        rentalId: 3,
        companyName: "City Tool Hire",
        customerName: "Amal Fernando",
        customerPhone: "0751234567",
        rentalDate: "2026-09-18",
        startDate: "2026-09-18",
        dueDate: "2026-09-24",
        rentalStatus: "OVERDUE",

        items: [
            {
                rentalItemId: 4,
                equipmentName: "Concrete Mixer",
                itemCode: "CM-001",
                quantityRented: 2,
                quantityAlreadyReturned: 0
            },
            {
                rentalItemId: 5,
                equipmentName: "Circular Saw",
                itemCode: "CS-001",
                quantityRented: 1,
                quantityAlreadyReturned: 0
            }
        ]
    }
];